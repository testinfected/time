package org.testinfected.time;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class Announcer<T> {

    public static <T> Announcer<T> to(Class<? extends T> listenerType) {
        return new Announcer<T>(listenerType);
    }

    private final T proxy;
    private final List<T> listeners = new ArrayList<T>();

    public Announcer(Class<? extends T> listenerType) {
        proxy = listenerType.cast(Proxy.newProxyInstance(
                listenerType.getClassLoader(),
                new Class<?>[]{listenerType},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        announce(method, args);
                        return null;
                    }
                }));
    }

    public void subscribe(T listener) {
        listeners.add(listener);
    }

    public boolean unsubscribe(T listener) {
        return listeners.remove(listener);
    }

    public T announce() {
        return proxy;
    }

    private void announce(Method m, Object[] args) throws Throwable {
        try {
            for (T listener : listeners) {
                m.invoke(listener, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}

