package com.inke.myeventbus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class EventBus {

    private Map<Object, List<SubscribleMethod>> cacheMap;
    private static volatile EventBus instance;
    private Handler mHandler;

    private EventBus() {
        cacheMap = new HashMap<>();
        mHandler = new Handler();
    }

    public static EventBus getDefault() {
        if(instance == null) {
            synchronized (EventBus.class) {
                if(instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public void register(Object obj) {
        //就是寻找obj (本例子中对应的就是MainActivity)中所有的带有subscribe注解的方法 放到map中管理
        List<SubscribleMethod> list = cacheMap.get(obj);
        if(list == null) {
            list = findSubscribeMethods(obj);
            cacheMap.put(obj, list);
        }
    }

    private List<SubscribleMethod> findSubscribeMethods(Object obj) {
        List<SubscribleMethod> list = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        while (clazz != null) {

            //凡是系统级别的父类，直接省略
            String name = clazz.getName();
            if(name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                break;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Subscrible subscribe = method.getAnnotation(Subscrible.class); //不为空
//            Override override = method.getAnnotation(Override.class); //永远都得不到
                if(subscribe == null) {
                    continue;
                }
                // 判断方法中的参数类型和个数
                Class<?>[] types = method.getParameterTypes();
                if(types.length != 1) {
                    Log.e("Error", "Eventbus only accept one para");
                }
                ThreadMode threadMode = subscribe.threadMode();
                SubscribleMethod subscribleMethod = new SubscribleMethod(method, threadMode, types[0]);
                list.add(subscribleMethod);
            }
            clazz = clazz.getSuperclass();
        }

        return list;
    }

    public void post(Object type) {
        // 直接循环cacheMap里的方法，找到对应就调用
        Set<Object> set = cacheMap.keySet();
        Iterator<Object> iterator = set.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            List<SubscribleMethod> list = cacheMap.get(obj);
            for (SubscribleMethod subscribleMethod : list) {
                // a (if条件前面的对象)对象所对应的类是不是b(if条件后面的对象)对象所对应的类信息的父类或者接口
                if(subscribleMethod.getType().isAssignableFrom(type.getClass())) {
                    switch (subscribleMethod.getThreadMode()) {
                        case MAIN:
                            // 主 - 主
                            if(Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(subscribleMethod, obj, type);
                            } else {
                                // 子 - 主
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod, obj, type);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            //子-子
                            invoke(subscribleMethod, obj, type);
                            //主-子
                            //ExecutorService
                            break;
                    }
                }
            }
        }
    }

    private void invoke(SubscribleMethod subscribleMethod, Object obj, Object type) {
         Method method = subscribleMethod.getMethod();
        try {
            method.invoke(obj, type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
