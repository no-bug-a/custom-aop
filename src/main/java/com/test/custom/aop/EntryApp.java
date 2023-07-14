package com.test.custom.aop;

import com.test.custom.aop.biz.IBaseBiz;
import com.test.custom.aop.biz.TestBiz;
import com.test.custom.aop.util.BeanUtil;

/**
 * @author zhangyi
 * @date 2023-07-14 10:05
 */
public class EntryApp {

    public static void main(String[] args) {

        BeanUtil.init(EntryApp.class);
        IBaseBiz bean = BeanUtil.getProxyBean(TestBiz.class);
        bean.hello();

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(bean.testAround());
    }
}
