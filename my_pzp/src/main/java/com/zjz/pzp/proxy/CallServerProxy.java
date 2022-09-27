package com.zjz.pzp.proxy;

import com.zjz.pzp.annotation.MyRequestMapper;
import com.zjz.pzp.pojo.CallServerBox;
import com.zjz.pzp.utils.AntPathMatcher;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zjz
 * @date 2022/9/9
 */
@Slf4j
public class CallServerProxy {

    private static AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 通过反射的方式 调用到传入的对象中 url地址匹配的方法
     *
     * @param targetObj 进行反射的对象，即最后执行的对象
     * @param request   netty传入的数据
     */
    public static CallServerBox callMapper(Object targetObj, FullHttpRequest request) {
        Validate.notNull(targetObj);
        Object returnObj = null;
        try {
            for (int i = 0; i < targetObj.getClass().getMethods().length; i++) {
                Method method = targetObj.getClass().getMethods()[i];
                MyRequestMapper mapper = method.getAnnotation(MyRequestMapper.class);
                if (mapper == null) {
                    continue;
                }
                String pathMapper = mapper.path();
                String head = mapper.headers();
                if (pathMatcher.match(pathMapper, request.uri())) {
                    // 匹配到对应的处理器
                    // 获取到所需要参数的对象信息
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0) {
                        if (FullHttpRequest.class.isAssignableFrom(parameterTypes[0])) {
                            returnObj = method.invoke(targetObj, request);
                            return new CallServerBox(head, (String) returnObj);
                        }
                        if (Map.class.isAssignableFrom(parameterTypes[0])) {
                            returnObj = method.invoke(targetObj, CallServerProxy.getRequestParams(request));
                            return new CallServerBox(head, (String) returnObj);
                        }
                    } else {
                        returnObj = method.invoke(targetObj);
                        return new CallServerBox(head, (String) returnObj);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new CallServerBox("text/html;", e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new CallServerBox("text/html;", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return new CallServerBox("text/html;", e.getMessage());
        }
        return new CallServerBox("text/html;", String.format("no found url :%s mapper", request.uri()));
    }

    /**
     * 把get请求的参数值转为map
     *
     * @param request
     * @return
     */
    private static Map<String, String> getRequestParams(FullHttpRequest request) throws IOException {
        HttpMethod method = request.method();
        Map<String, String> parmMap = new HashMap<>();
        if (HttpMethod.GET == method) {
            // 是GET请求
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            decoder.parameters().entrySet().forEach(entry -> {
                // entry.getValue()是一个List, 只取第一个元素
                parmMap.put(entry.getKey(), entry.getValue().get(0));
            });
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            decoder.offer(request);

            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

            for (InterfaceHttpData parm : parmList) {

                Attribute data = (Attribute) parm;
                parmMap.put(data.getName(), data.getValue());
            }
        } else {
            // 不支持其它方法
            throw new IllegalAccessError("http is not supper"); // 这是个自定义的异常, 可删掉这一行
        }
        return parmMap;
    }
}
