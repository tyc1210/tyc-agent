package com.tyc.agent;


import com.sun.tools.attach.VirtualMachine;

import java.lang.instrument.Instrumentation;


/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2024-07-22 15:59:50
 */
public class AttachMain {

    public static void agentmain(String agentArgs, Instrumentation inst){
        System.out.println("[agentmain 执行了][参数："+agentArgs+"]");
    }

    public static void main(String[] args) {
        try {
            // 参数为要监控的应用程序ID
            VirtualMachine attach = VirtualMachine.attach("15540");
            // 执行 java agent 里面的agentmain 方法（所以需要找到jar包的路径）
            attach.loadAgent("E:\\javaProjects\\tyc-agent\\target\\tyc-agent.jar","1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
