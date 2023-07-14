package com.test.custom.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class ProxyFactory {

    public static <T> T getBeanBaseProxy(Class<T> clazz) {
        return getCglibProxy(clazz, new ProxyBeanBaseInterceptor(clazz));
    }

    public static <T> T getCglibProxy(Class<T> clazz, MethodInterceptor methodInterceptor) {
        // 创建动态代理增强类
        Enhancer enhancer = new Enhancer();
        // 设置类加载器
        enhancer.setClassLoader(clazz.getClassLoader());
        // 设置被代理类
        enhancer.setSuperclass(clazz);
        // 设置方法拦截器
        enhancer.setCallback(methodInterceptor);
        // 创建代理类
        return clazz.cast(enhancer.create());
    }

}
