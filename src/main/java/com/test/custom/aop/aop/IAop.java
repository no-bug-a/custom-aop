package com.test.custom.aop.aop;

import com.test.custom.aop.proxy.ProxyJob;

/**
 * @author zhangyi
 * @date 2023-07-14 15:09
 */
public interface IAop {
    void before(ProxyJob proxyJob);
    Object around(ProxyJob proxyJob);
    void after(ProxyJob proxyJob);
}
