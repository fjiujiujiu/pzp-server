package com.zjz.pzpS;

import com.zjz.pzpS.server.UdpCenterServer;
import org.apache.commons.cli.*;

/**
 * @author zjz
 * @date 2022/9/11
 */
public class pzpServerApplication {
    public static void main(String[] args) {
        // 根据命令行参数定义Option对象，第1/2/3/4个参数分别是指命令行参数名缩写、参数名全称、是否有参数值、参数描述
        Option opt1 = new Option("port","port",true,"udp服务启动的端口号");
        opt1.setRequired(false);
        Options options = new Options();
        options.addOption(opt1);
        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            cli = cliParser.parse(options, args);
            int udp = Integer.parseInt(cli.getOptionValue("port","8887"));
            // 进行用户前线服务的运行
            new UdpCenterServer().run(udp);
        } catch (ParseException e) {
            // 解析失败是用 HelpFormatter 打印 帮助信息
            helpFormatter.printHelp(">>>>>> options", options);
            e.printStackTrace();
        }
    }
}
