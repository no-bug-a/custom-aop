package com.test.custom.aop.biz;

/**
 * @author zhangyi
 * @date 2023-07-14 10:11
 */
public class TestBiz implements IBaseBiz{

    public void hello() {
        System.out.println("hello world!");
    }

    @Override
    public String testBefore() {
        return "before";
    }

    @Override
    public String testAround() {
        return "around";
    }

    @Override
    public String testAfter() {
        return "after";
    }

}
