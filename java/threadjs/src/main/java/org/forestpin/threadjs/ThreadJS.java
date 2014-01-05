package org.forestpin.threadjs;

import java.lang.reflect.InvocationTargetException;

import net.sf.json.JSONObject;

public class ThreadJS {
    private String host = "localhost";
    private int port = 11010;

    private ThreadJSSocket socket;

    ThreadJS() {
        init();
    }

    ThreadJS(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        socket = new ThreadJSSocket(this, host, port);
    }

    public void onMessage(JSONObject msg, Callback cb) {
        // TODO call the callback obj in case of callback. If not send call the
        // method!
        String methodName = msg.optString("method", null);
        JSONObject data = msg.optJSONObject("data");
        if (methodName == null) {
            System.err.println("No method in reqest");
        }

        java.lang.reflect.Method method = null;
        try {
            method = this.getClass().getMethod(methodName, JSONObject.class, Callback.class);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method != null) {
            try {
                method.invoke(data, cb);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void testMethod(JSONObject data, Callback cb) {
        cb.callback(null, data);
    }

    public void _ping(JSONObject data, Callback cb) {
        cb.callback(null, data);
    }

    public void _string(JSONObject data, Callback cb) {
        cb.callback(null, data);
    }

}