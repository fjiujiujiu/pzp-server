package com.zjz.pzp;

import ch.qos.logback.core.util.TimeUtil;
import com.zjz.pzp.webServe.MyUDPServe;
import com.zjz.pzp.webServe.MyWebServe;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author zjz
 * @date 2022/9/2
 */
@Slf4j
public class PzpApplication {
    public static void main(String[] args) {
        // 根据命令行参数定义Option对象，第1/2/3/4个参数分别是指命令行参数名缩写、参数名全称、是否有参数值、参数描述
        Option opt1 = new Option("http","http_port",true,"http服务启动的端口号");
        opt1.setRequired(false);
        Option opt2 = new Option("udp","udp_port",true,"udp服务启动的端口号");
        opt2.setRequired(false);
        Options options = new Options();
        options.addOption(opt1);
        options.addOption(opt2);
        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {

            cli = cliParser.parse(options, args);
            int udp = Integer.parseInt(cli.getOptionValue("udp","9998"));
            int tcp = Integer.parseInt(cli.getOptionValue("http","9995"));
            // 启动udp服务同时设置其为main线程的守护线程
            Thread udpThread = new Thread(() ->{
                MyUDPServe.run(udp);
            });
            udpThread.setDaemon(true);
            udpThread.start();
            // 启动http 服务
            new MyWebServe().run(tcp, "/ssssss");
        } catch (ParseException e){
            // 解析失败是用 HelpFormatter 打印 帮助信息
            helpFormatter.printHelp(">>>>>> options", options);
            e.printStackTrace();
        }catch (InterruptedException e) {
            log.error("PzpApplication", e);
            e.printStackTrace();
        }
    }
}
