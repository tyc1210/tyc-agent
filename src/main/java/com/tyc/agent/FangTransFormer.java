package com.tyc.agent;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.TimeUnit;

/**
 * 类描述
 *
 * @author tyc
 * @version 1.0
 * @date 2024-07-22 16:45:32
 */
public class FangTransFormer implements ClassFileTransformer {

    /**
     * @param classfileBuffer 当前类文件字节数组
     * @return 返回一个新的字节数组，该数组包含经过转换的类文件内容
     * @throws IllegalClassFormatException
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if(!(className.contains("com/jiufang") && className.contains("ServiceImpl") && !className.contains("$$"))){
            return classfileBuffer;
        }
        try {
            System.out.println("[转换目标类：]"+className);
            return agentForRunTimeStatistical(loader,className);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对目标方法进行运行时长记录
     */
    private byte[] agentForRunTimeStatistical(ClassLoader loader,String className) throws Exception {
        String classPath = className.replaceAll("/",".");
        CtClass ctClass = this.findClassInClassPool(loader,classPath,true);
        if(ctClass == null){
            System.out.println("在类加载器中找不到："+classPath);
            return null;
        }
        System.out.println("在类加载器中找到了："+classPath);
        // 只对本类方法进行拦截，不处理父类方法
        CtMethod[] methods = ctClass.getDeclaredMethods();

        for (CtMethod method : methods) {
            CtMethod methodCopy = CtNewMethod.copy(method, ctClass, new ClassMap());
            String agentMethodName = method.getName() + "$agent";
            method.setName(agentMethodName);
            String fullMethodName = classPath + "."+methodCopy.getName();
            StringBuffer body = new StringBuffer("{\n")
                    .append("long begin = System.currentTimeMillis();\n")
                    .append("try{\n")
                    .append("return ($r)" + agentMethodName + "($$);\n") // ($$) 表示方法入参  ($r) 表示方法返回值
                    .append("}finally {\n")
                    .append("long executeTime = System.currentTimeMillis() - begin;\n")
                    .append("System.out.println(\"" + fullMethodName + " method total execute time is \" + executeTime);\n")
                    .append("}\n")
                    .append("}");
            methodCopy.setBody(body.toString());
            ctClass.addMethod(methodCopy);
        }
        return ctClass.toBytecode();
    }


    /**
     * 直接在默认的 ClassPool 中查找类。默认的 ClassPool 可能已经包含了一些常用的类，但并不一定包含目标类
     * 第二次查找是在添加了目标类的类加载器后的 ClassPool 中查找,因为目标类可能是在运行时动态加载的，而默认的 ClassPool 并不知道这些动态加载的类
     */
    private CtClass findClassInClassPool(ClassLoader loader, String classPath, boolean firstTry) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = null;
        try {
            ctClass = pool.get(classPath);
        }catch (NotFoundException e){
            if(firstTry){
                System.out.println("can not find "+ classPath +" in ClassPool first time");
                pool.appendClassPath(new LoaderClassPath(loader));
                ctClass = findClassInClassPool(loader,classPath,false);
            }else {
                e.printStackTrace();
            }
        }
        return ctClass;
    }
}
