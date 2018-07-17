package tsou.cn.lib_bus;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2018/7/17 0017.
 */

public class HxgBus {

    /**
     * subscriptionsByEventType 集合存放，主要存放当前类的被注释的方法的一些信息
     * <p>
     * key 是注解的方法“参数”的类Class
     * value 存放的是 Subscription 的集合列表
     * Subscription 包含两个属性，一个是 subscriber 订阅者（反射执行对象），一个是SubscriberMethod 注解方法所有属性参数值
     */
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    /**
     * typesBySubscriber 集合存放，这个方法主要是为了删除用的
     * <p>
     * key 当前类的对象
     * value 是所有订阅者里面方法的参数的class
     */
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    private HxgBus() {
        typesBySubscriber = new HashMap<>();
        subscriptionsByEventType = new HashMap<>();
    }

    static volatile HxgBus defaultInstance;

    public static HxgBus getDefault() {
        if (defaultInstance == null) {
            synchronized (HxgBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new HxgBus();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * 注册
     *
     * @param object
     */
    public void register(Object object) {
        //1.解析所有方法封装成 SubscriberMethod 的集合
        List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        Class<?> objectClass = object.getClass();
        Method[] methods = objectClass.getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                /**
                 * 所有的Subscribe属性 解析出来
                 */
                //得到所有的参数类型
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes == null || parameterTypes.length == 0) {
                    throw new RuntimeException("Subscribe注解方法参数不能为空");
                }
                SubscriberMethod subscriberMethod = new SubscriberMethod(
                        method, parameterTypes[0], subscribe.threadMode(),
                        subscribe.priority());
                subscriberMethods.add(subscriberMethod);
            }
        }
        //2.按照规则存放到subscriptionsByEventType中
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscriber(object, subscriberMethod);
        }
    }

    //2.按照规则存放到subscriptionsByEventType中
    private void subscriber(Object object, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        }
        //判断优先级
        Subscription newSubscription = new Subscription(object, subscriberMethod);
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

        // typesBySubscriber 方便移除
        List<Class<?>> eventTypes = typesBySubscriber.get(object);
        if (eventTypes == null) {
            eventTypes = new ArrayList<>();
            typesBySubscriber.put(object, eventTypes);
        }
        if (!eventTypes.contains(eventType)) {
            eventTypes.add(eventType);
        }
    }

    /**
     * 移除
     *
     * @param object
     */
    public void unregister(Object object) {
        List<Class<?>> eventTypes = typesBySubscriber.get(object);
        if (eventTypes != null) {
            for (Class<?> eventType : eventTypes) {
                removeObject(eventType, object);
            }
        }
    }

    private void removeObject(Class<?> eventType, Object object) {
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            int size = subscriptions.size();
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                if (subscription.subscriber == object) {
                    //将订阅信息从集合中移除
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }

    /**
     * 发送信息
     *
     * @param event 通过参数的class来查找对应的注释方法
     */
    public void post(Object event) {
        //遍历 subscriptionsByEventType，找到符合的方法调用方法的 method.invoke()执行，并判断是哪个线程
        Class<?> eventType = event.getClass();
        //找到符合的方法调用方法的 method.invoke()执行
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                executeMethod(subscription, event);
            }
        }
    }

    private void executeMethod(final Subscription subscription, final Object event) {
        ThreadMode threadMode = subscription.subscriberMethod.threadMode;
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        switch (threadMode) {
            case POSTING://相同线程
                invoMethod(subscription, event);
                break;
            case MAIN://主线程
                if (isMainThread) {
                    invoMethod(subscription, event);
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            invoMethod(subscription, event);
                        }
                    });
                }
                break;
            case ASYNC://异步线程
                AsyncPoster.enqueue(subscription, event);
                break;
            case BACKGROUND://子线程
                if (!isMainThread) {
                    invoMethod(subscription, event);
                } else {
                    AsyncPoster.enqueue(subscription, event);
                }
                break;
        }
    }

    private void invoMethod(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
