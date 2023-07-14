package com.test.custom.aop.aop;

import com.test.custom.aop.ann.*;
import com.test.custom.aop.proxy.ProxyJob;

/**
 * @author zhangyi
 * @date 2023-07-14 11:19
 */
@Aop
public class TestAop2 implements IAop{

    @Pointcut("com.test.custom.aop.biz.TestBiz.*")
    public void point(){}

    @Before("point()")
    public void before(ProxyJob proxyJob) {
        System.out.println("TestAop2 before " + proxyJob.getMetaMethod().getName());
    }

    @Around("point()")
    public Object around(ProxyJob proxyJob) {
        System.out.println("TestAop2 around before " + proxyJob.getMetaMethod().getName());
        Object invoke = proxyJob.invoke();
        System.out.println("TestAop2 around after " + proxyJob.getMetaMethod().getName());
        return invoke;
    }

    @After("point()")
    public void after(ProxyJob proxyJob) {
        System.out.println("TestAop2 after " + proxyJob.getMetaMethod().getName());
    }

}
