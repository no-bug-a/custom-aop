package com.test.custom.aop.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author zhangyi
 * @date 2023-07-14 15:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyJob {

    private Object object;

    private MethodProxy method;

    private Method metaMethod;

    private Object[] args;

    public Object invoke() {
        try {
            return method.invokeSuper(object, args);
        } catch (Throwable e) {
            throw new RuntimeException("proxy invoke failure: ", e);
        }
    }

}
