package com.inke.myeventbus;

import java.lang.reflect.Method;

public class SubscribleMethod {

    // 方法本身
    private Method method;
    // 线程模式
    private ThreadMode threadMode;
    // 方法中的参数类型
    private Class<?> type;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public SubscribleMethod(Method method, ThreadMode threadMode, Class<?> type) {
        this.method = method;
        this.threadMode = threadMode;
        this.type = type;
    }

}
