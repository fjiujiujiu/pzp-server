package com.zjz.pzp.controller;

import com.alibaba.fastjson.JSONObject;
import com.zjz.pzp.annotation.MyRequestMapper;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zjz
 * @date 2022/9/9
 */
@Slf4j
public class MyServerController {
    private int port;

    public MyServerController(int port) {
        this.port = port;
    }

    /**
     * 获取首页内容
     * @param req
     * @return
     */
    @MyRequestMapper(path = "/index",headers="text/html;")
    public String index(FullHttpRequest req) {

        // 获取HTML文件流
        StringBuffer htmlSb = new StringBuffer();
        try {
            InputStream path = this.getClass().getResourceAsStream("/static/dist/index.html");
            BufferedReader br = new BufferedReader(new InputStreamReader(path));
            while (br.ready()) {
                htmlSb.append(br.readLine());
            }
            br.close();
            // 删除临时文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HTML文件字符串
        String htmlStr = String.format(htmlSb.toString(), port);
        // 返回经过清洁的html文本
        return htmlStr;
    }
    @MyRequestMapper(path = "/js/*",headers="application/javascript;")
    public String js(FullHttpRequest req){
        // 获取HTML文件流
        StringBuffer htmlSb = new StringBuffer();
        try {
            InputStream path = this.getClass().getResourceAsStream("/static/dist"+req.uri());
            BufferedReader br = new BufferedReader(new InputStreamReader(path));
            while (br.ready()) {
                htmlSb.append(br.readLine());
            }
            br.close();
            // 删除临时文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HTML文件字符串
        String htmlStr = htmlSb.toString();
        // 返回经过清洁的html文本
        return htmlStr;
    }

    @MyRequestMapper(path = "/css/*",headers="text/css;")
    public String css(FullHttpRequest req) {
        // 获取HTML文件流
        StringBuffer htmlSb = new StringBuffer();
        try {
            InputStream path = this.getClass().getResourceAsStream("/static/dist"+req.uri());
            BufferedReader br = new BufferedReader(new InputStreamReader(path));
            while (br.ready()) {
                htmlSb.append(br.readLine());
            }
            br.close();
            // 删除临时文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HTML文件字符串
        String htmlStr = htmlSb.toString();
        // 返回经过清洁的html文本
        return htmlStr;
    }

    @MyRequestMapper(path = "/favicon.ico",headers="image/x-icon;")
    public String ico(FullHttpRequest req) {
        // 获取HTML文件流
        StringBuffer htmlSb = new StringBuffer();
        try {
            InputStream path = this.getClass().getResourceAsStream("/static/dist"+req.uri());
            BufferedReader br = new BufferedReader(new InputStreamReader(path));
            while (br.ready()) {
                htmlSb.append(br.readLine());
            }
            br.close();
            // 删除临时文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HTML文件字符串
        String htmlStr = htmlSb.toString();
        // 返回经过清洁的html文本
        return htmlStr;
    }
    @MyRequestMapper(path = "/filessssPath")
    public String getIndex(FullHttpRequest req) {
        return "123232";
    }
    @MyRequestMapper(path = "/api/hasFile?*")
    public String hasFile(Map<String, String> map) {
        String filePath = map.get("filePath");
        log.info("查询路径{}是否有文件", filePath);
        File file = new File(filePath);
        Map<String, Boolean> v = new HashMap<>();
        v.put("res", file.exists());
        return JSONObject.toJSONString(v);
    }

}
