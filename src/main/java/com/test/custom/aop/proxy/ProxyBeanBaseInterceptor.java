package com.test.custom.aop.proxy;

import cn.hutool.core.util.ReflectUtil;
import com.test.custom.aop.util.BeanUtil;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;
import java.util.List;

public class ProxyBeanBaseInterceptor implements MethodInterceptor {

    private final Class<?> clazz;

    public ProxyBeanBaseInterceptor(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param o           被代理的对象（需要增强的对象）
     * @param method      被拦截的方法（需要增强的方法）
     * @param args        方法入参
     * @param methodProxy 用于调用原始方法
     */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        String methodName = clazz.getName() + "." + method.getName();
        ProxyJob proxyJob = new ProxyJob(o, methodProxy, method, args);
        invokeBefore(methodName, proxyJob);
        Object object = invokeAround(methodName, proxyJob);
        invokeAfter(methodName, proxyJob);
        return object;
    }

    private void invokeBefore(String methodName, ProxyJob proxyJob) {
        List<Method> beforeMethods = BeanUtil.getBeforeMethods(methodName);
        for (Method beforeMethod : beforeMethods) {
            ReflectUtil.invoke(BeanUtil.getAopInstance(beforeMethod.getDeclaringClass().getName()), beforeMethod, proxyJob);
        }
    }

    private Object invokeAround(String methodName, ProxyJob proxyJob) {
        List<Method> aroundMethods = BeanUtil.getAroundMethods(methodName);
        if (aroundMethods.isEmpty()) {
            return proxyJob.invoke();
        }
        Object result = null;
        for (Method aroundMethod : aroundMethods) {
            result = ReflectUtil.invoke(BeanUtil.getAopInstance(aroundMethod.getDeclaringClass().getName()), aroundMethod, proxyJob);
        }
        return result;
    }

    private void invokeAfter(String methodName, ProxyJob proxyJob) {
        List<Method> afterMethods = BeanUtil.getAfterMethods(methodName);
        for (Method afterMethod : afterMethods) {
            ReflectUtil.invoke(BeanUtil.getAopInstance(afterMethod.getDeclaringClass().getName()), afterMethod, proxyJob);
        }
    }

}
