package com.test.custom.aop.util;

import cn.hutool.core.util.ReflectUtil;
import com.test.custom.aop.EntryApp;
import com.test.custom.aop.ann.*;
import com.test.custom.aop.proxy.ProxyFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author zhangyi
 * @date 2023-07-14 10:12
 */
public class BeanUtil {

    private static final List<Method> beforeMethods = new ArrayList<>();
    private static final List<Method> aroundMethods = new ArrayList<>();
    private static final List<Method> afterMethods = new ArrayList<>();

    private static final Map<String, List<Method>> pointcutMethodMap = new HashMap<>();
    private static final Map<String, List<Method>> beforeMethodMap = new HashMap<>();
    private static final Map<String, List<Method>> aroundMethodMap = new HashMap<>();
    private static final Map<String, List<Method>> afterMethodMap = new HashMap<>();
    private static final Map<String, Object> aopInstanceMap = new HashMap<>();

    public static <T> T getProxyBean(Class<T> clazz) {
        return ProxyFactory.getBeanBaseProxy(clazz);
    }

    public static List<Class<?>> scanClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(packagePath);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            try {
                scanClassesInDirectory(packageName, file, classes);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return classes;
    }

    private static void scanClassesInDirectory(String packageName, File directory, List<Class<?>> classes)
            throws ClassNotFoundException {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanClassesInDirectory(packageName + "." + file.getName(), file, classes);
                    } else if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    }
                }
            }
        }
    }

    public static void init(Class<EntryApp> entryAppClass) {
        List<Class<?>> classList = scanClasses(entryAppClass.getPackage().getName());
        //扫描所有的Aop代理方法
        for (Class<?> clazz : classList) {
            scanAop(clazz);
        }
        //填充被代理方法所要调用的Aop代理方法
        for (Class<?> clazz : classList) {
            initProxyMethodAopMap(clazz);
        }
    }

    private static void initProxyMethodAopMap(Class<?> clazz) {
        Method[] methods = ReflectUtil.getMethods(clazz);
        for (Method method : methods) {
            initProxyMethodBeforeMap(clazz, method);
            initProxyMethodAroundMap(clazz, method);
            initProxyMethodAfterMap(clazz, method);
        }
    }


    private static void initProxyMethodBeforeMap(Class<?> clazz, Method method) {
        for (Method beforeMethod : beforeMethods) {
            String regex = getRegex(beforeMethod, beforeMethod.getAnnotation(Before.class).value());
            setMethodMap(clazz, method, beforeMethod, regex, beforeMethodMap);
        }
    }

    private static void initProxyMethodAroundMap(Class<?> clazz, Method method) {
        for (Method aroundMethod : aroundMethods) {
            String regex = getRegex(aroundMethod, aroundMethod.getAnnotation(Around.class).value());
            setMethodMap(clazz, method, aroundMethod, regex, aroundMethodMap);
        }
    }

    private static void initProxyMethodAfterMap(Class<?> clazz, Method method) {
        for (Method afterMethod : afterMethods) {
            String regex = getRegex(afterMethod, afterMethod.getAnnotation(After.class).value());
            setMethodMap(clazz, method, afterMethod, regex, afterMethodMap);
        }
    }


    private static String getRegex(Method method, String value) {
        List<Method> methods = pointcutMethodMap.getOrDefault(method.getDeclaringClass().getName(), Collections.emptyList());
        String pointMethodName = value.replaceAll("\\(\\)", "");
        for (Method pointcutMethod : methods) {
            if (pointcutMethod.getName().equals(pointMethodName)) {
                return regexParse(pointcutMethod.getAnnotation(Pointcut.class).value());
            }
        }
        return regexParse(value);
    }

    private static String regexParse(String value) {
        return value.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
    }

    private static void setMethodMap(Class<?> clazz, Method method, Method proxyMethod, String regex, Map<String, List<Method>> methodMap) {
        if (regex == null) {
            return;
        }
        String methodName = clazz.getName() + "." + method.getName();
        if (methodName.matches(regex)) {
            List<Method> methods = methodMap.get(methodName);
            if (methods == null) {
                methods = new ArrayList<>();
            }
            methods.add(proxyMethod);
            methodMap.putIfAbsent(methodName, methods);
        }
    }


    private static void scanAop(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Aop.class)) {
            return;
        }
        Method[] methods = ReflectUtil.getMethods(clazz);
        Object aopInstance = ReflectUtil.newInstance(clazz);
        aopInstanceMap.putIfAbsent(clazz.getName(), aopInstance);
        for (Method method : methods) {
            scanPointcut(method);
            scanBefore(method);
            scanAround(method);
            scanAfter(method);
        }
    }

    private static void scanPointcut(Method method) {
        if (!method.isAnnotationPresent(Pointcut.class)) {
            return;
        }
        List<Method> methods = pointcutMethodMap.getOrDefault(method.getDeclaringClass().getName(), new ArrayList<>());
        methods.add(method);
        pointcutMethodMap.putIfAbsent(method.getDeclaringClass().getName(), methods);
    }

    private static void scanBefore(Method method) {
        if (!method.isAnnotationPresent(Before.class)) {
            return;
        }
        beforeMethods.add(method);
    }

    private static void scanAround(Method method) {
        if (!method.isAnnotationPresent(Around.class)) {
            return;
        }
        aroundMethods.add(method);
    }

    private static void scanAfter(Method method) {
        if (!method.isAnnotationPresent(After.class)) {
            return;
        }
        afterMethods.add(method);
    }

    public static List<Method> getBeforeMethods(String methodName) {
        return beforeMethodMap.getOrDefault(methodName, Collections.emptyList());
    }

    public static List<Method> getAroundMethods(String methodName) {
        return aroundMethodMap.getOrDefault(methodName, Collections.emptyList());
    }

    public static List<Method> getAfterMethods(String methodName) {
        return afterMethodMap.getOrDefault(methodName, Collections.emptyList());
    }

    public static Object getAopInstance(String className) {
        return aopInstanceMap.get(className);
    }
}
