package com.test.custom.aop.biz;

/**
 * @author zhangyi
 * @date 2023-07-14 10:40
 */
public interface IBaseBiz {
    void hello();
    String testBefore();
    String testAround();
    String testAfter();
}
