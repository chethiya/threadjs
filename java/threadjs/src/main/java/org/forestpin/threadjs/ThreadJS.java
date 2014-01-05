package org.forestpin.threadjs;

import java.lang.reflect.InvocationTargetException;

import net.sf.json.JSONObject;

public class ThreadJS {
    private String host = "localhost";
    private int port = 11010;

    private final String PING = "_ping";
    private final String START = "_start";

    private ThreadJSSocket socket;

    private boolean started = false;
    private Callback startedCb = null;

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

        while (!started) {
            send(PING, new JSONObject(), new Callback() {
                public void callback(String err, JSONObject data) {
                    if (err == null) {
                        send(START, new JSONObject(), new Callback() {
                            public void callback(String err, JSONObject data) {
                                if (err == null) {
                                    started = true;
                                    if (startedCb != null) {
                                        startedCb.callback(err, data);
                                    }
                                }
                            }
                        });
                    }
                }
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
            System.err.println("Invalid method : " + methodName);
            e.printStackTrace();
        }
        if (method != null) {
            try {
                method.invoke(this, data, cb);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String method, JSONObject data, Callback cb) {
        if (!socket.isConnected()) {
            cb.callback("Socket is not connected", data);
            return;
        } else {
            JSONObject msg = new JSONObject();
            msg.element("method", method);
            msg.element("data", data);
            socket.send(msg, cb);
        }
    }

    public void onStarted(Callback cb) {
        if (started) {
            cb.callback(null, null);
        } else {
            startedCb = cb;
        }
    }

    public void testMethod(JSONObject data, Callback cb) {
        cb.callback(null, data);
    }

    public void _ping(JSONObject data, Callback cb) {
        System.out.println("[java] Ping is called");
        cb.callback(null, data);
    }

    public void _start(JSONObject data, Callback cb) {
        System.out.println("[java] Start is called");
        cb.callback(null, data);
    }

    public void getRecord(JSONObject data, Callback cb) {
        System.out.println("[java] getRecord is called");
        cb.callback(null, data);

        JSONObject obj = JSONObject.fromObject("{vendor: \"pamona\", amount: 12313}");
        for (int i=0; i<10000; ++i) {
            send("onRecord", obj, null);
        }
    }

}