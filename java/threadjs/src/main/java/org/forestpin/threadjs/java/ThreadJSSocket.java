package org.forestpin.threadjs.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class ThreadJSSocket {
    private String host = "localhost";
    private int port = 11010;
    private final String _START = "[START]";
    private final String _END = "[END]";
    private final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";

    private ThreadJS main = null;
    private Socket socket = null;
    private PrintWriter out; // sync
    private BufferedReader in;
    Thread inThread = null;

    private final MessageThrottler throttler;

    private boolean connected = false;
    HashMap<String, Callback> calls = new HashMap<String, Callback>(); // sync

    ThreadJSSocket(ThreadJS main, String host, int port, int maxThreads) {
        this.main = main;
        this.host = host;
        this.port = port;
        throttler = new MessageThrottler(maxThreads);
        throttler.start();
        init();
    }

    private String getRandomString() {
        return getRandomString(32);
    }

    private String getRandomString(int length) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < 32; ++i) {
            int p = (int) Math.floor(Math.random() * length);
            str.append(CHARS.charAt(p));
        }
        return str.toString();
    }

    private synchronized String getId() {
        String res;
        synchronized (calls) {
            while (true) {
                res = getRandomString();
                if (calls.get(res) == null)
                    break;
            }
        }

        return res;
    }

    private boolean isNewMessage(String str) {
        int len = _START.length();
        if (str.length() < len) {
            return false;
        } else if (_START.equals(str.substring(0, len))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isEndMessage(String str) {
        int len = _END.length();
        int slen = str.length();
        if (slen < len) {
            return false;
        } else if (_END.equals(str.substring(slen - len))) {
            return true;
        } else {
            return false;
        }
    }

    private void onMessage(String msg) {
        // TODO call the callback obj in case of callback. If not send call the
        // method!
        JSONObject json = JSONObject.fromObject(msg);
        if (!json.has("callback")) {
            System.err.println("No callback: " + msg);
            return;
        }
        boolean callback = json.optBoolean("callback");
        final String reqId = json.optString("reqId", null);
        final JSONObject data = json.optJSONObject("data");
        if (data == null) {
            System.err.println("No data object in : " + msg);
            return;
        }
        if (callback) {
            final Callback cb;
            synchronized (calls) {
                cb = calls.get(reqId);
            }
            if (reqId != null && cb != null) {
                final String err;
                if (data.optJSONObject("err") == null) {
                    err = null;
                } else {
                    err = data.optString("err", null);
                }
                final JSONObject d = data.optJSONObject("data");
                if (cb != Callback.EMPTY) {
                    Thread newThread = new Thread() {
                        @Override
                        public void run() {
                            cb.callback(err, d);
                        }
                    };
                    newThread.start();
                }
                synchronized (calls) {
                    calls.remove(reqId);
                }
            } else {
                System.err.println("There is no callback for reqId: " + reqId);
            }
        } else {
            Thread newProcess = new Thread() {
                @Override
                public void run() {
                    main.onMessage(data, new Callback() {
                        public void callback(String err, JSONObject data) {
                            JSONObject m = new JSONObject();
                            JSONObject d = new JSONObject();

                            d.element("err", (err == null ? JSONNull.getInstance() : err));
                            d.element("data", data);
                            m.element("callback", true);
                            m.element("reqId", reqId);
                            m.element("data", d);
                            placeInOut(m);

                            // Callback is the point of exit. Therefore we can
                            // assume at this point thread execution is over
                            throttler.onThreadExit();
                        }
                    });
                }
            };
            throttler.push(newProcess);
        }
    }

    private void init() {
        while (true) {
            try {

                socket = new Socket(host, port);
                System.out.println("Connected to nodejs");
                break;
            }

            catch (IOException e) {
                System.out.println("Nodejs socket is not up. Will be trying in 5 seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        try {
            // TODO: Set encoding to utf8
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            startInThread();
        } catch (IOException e) {
            System.err.println("Error while establishing input/output streams to nodejs");
            e.printStackTrace();
        }

    }

    private void startInThread() {
        System.out.println("calling new thread");
        inThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Starting read thread");
                try {
                    // TODO using a hacky algorithm to decode message. Using new
                    // line
                    // characters assuming there are new line character at the
                    // end of
                    // each message
                    boolean started = false;
                    String str;
                    String msg = "";
                    while ((str = in.readLine()) != null) {
                        if (isNewMessage(str)) {
                            msg = "";
                            str = str.substring(_START.length());
                            started = true;
                        }
                        if (!started)
                            continue;

                        if (isEndMessage(str)) {
                            str = str.substring(0, str.length() - _END.length());
                            msg += str;
                            onMessage(msg);
                            started = false;
                        } else
                            msg += str;
                    }
                    close();
                    System.out.println("Nodejs has closed the socket");
                } catch (IOException e) {
                    System.err.println("Error while reading from nodejs");
                    e.printStackTrace();
                }
            }
        };
    }

    public void start() {
        if (inThread == null) {
            System.err.println("Can't start hearing on socket. Socket is not connected ");
        } else {
            connected = true;
            inThread.start();
        }
    }

    // TODO debug _start not going out
    private void placeInOut(JSONObject msg) {
        String str = msg.toString();
        synchronized (out) {
            out.println(_START + str + _END);
        }
    }

    private void close() {
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public void send(JSONObject json, Callback cb) {
        if (!connected) {
            System.err.println("Channel is closed. Can't send : " + json.toString());
            return;
        }
        JSONObject msg = new JSONObject();
        String reqId = getId();
        msg.element("callback", false);
        msg.element("reqId", reqId);
        msg.element("data", json);
        if (cb == null) {
            cb = Callback.EMPTY;
        }
        synchronized (calls) {
            calls.put(reqId, cb);
        }
        placeInOut(msg);
    }
}
