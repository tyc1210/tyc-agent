package com.tyc.agent;

import java.lang.instrument.Instrumentation;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2024-07-22 15:22:10
 */
public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[agent remain 执行]");
        inst.addTransformer(new FangTransFormer());
    }




}
