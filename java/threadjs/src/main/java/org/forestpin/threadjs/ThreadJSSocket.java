package org.forestpin.threadjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

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
    Thread inThread, outThread;

    private boolean started = false;
    HashMap<String, Callback> calls = new HashMap<String, Callback>(); //sync

    ThreadJSSocket(ThreadJS main, String host, int port) {
        this.main = main;
        this.host = host;
        this.port = port;
        init();
    }

    private String getRandomString() {
        return getRandomString(32);
    }

    private String getRandomString(int length) {
        StringBuffer str = new StringBuffer();
        for (int i=0; i<32; ++i) {
            int p = (int) Math.floor(Math.random()*length);
            str.append(CHARS.charAt(p));
        }
        return str.toString();
    }

    private synchronized String getId() {
        String res;
        while (true) {
            res = getRandomString();
            if (calls.get(res) == null) break;
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
        System.out.println("Messsage received: " + msg);
        JSONObject json = JSONObject.fromObject(msg);
        if (!json.has("callback")) {
            System.err.println("No callback: " + msg);
            return;
        }
        boolean callback = json.optBoolean("callback");
        final String reqId = json.optString("reqId", null);
        JSONObject data = json.optJSONObject("data");
        if (data == null) {
            System.err.println("No data object in : " + msg);
            return;
        }
        if (callback) {
            if (reqId != null && calls.get(reqId) != null) {
                Callback cb = calls.get(reqId);
                String err = data.optString("err", null);
                JSONObject d = data.optJSONObject("data");
                cb.callback(err, d);
                calls.remove(reqId);
            } else {
                System.err.println("There is no callback for reqId: " + reqId);
            }
        } else {
            main.onMessage(data, new Callback() {
                public void callback(String err, JSONObject data) {
                    JSONObject m = new JSONObject();
                    JSONObject d = new JSONObject();
                    d.element("err", err);
                    d.element("data", data);
                    m.element("callback", true);
                    m.element("reqId", reqId);
                    m.element("data", d);
                    placeInOut(m);
                }
            });
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
            //TODO: Set encoding to utf8
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            started = true;
        } catch (IOException e) {
            System.err.println("Error while establishing input/output streams to nodejs");
            e.printStackTrace();
        }
        startInThread();
        startOutThread();
    }

    private void startInThread() {
        inThread = new Thread() {
            @Override
            public void run() {
                try {
                    // TODO using a hacky algorithm to decode message. Using new line
                    // characters assuming there are new line character at the end of
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

    private void startOutThread() {
        outThread = new Thread() {
            @Override
            public void run() {

            }
        };
    }

    private synchronized void placeInOut(JSONObject msg) {
        String str = msg.toString();
        out.print(_START + str + _END + "\n");
    }

    private void close() {
        started = false;
    }

    public void send(JSONObject json, Callback cb) {
        if (!started) {
            System.err.println("Channel is closed. Can't send : " + json.toString());
            return;
        }
        JSONObject msg = new JSONObject();
        String reqId = getId();
        msg.element("callback", false);
        msg.element("reqId", reqId);
        msg.element("data", json);
        calls.put(reqId, cb);
        placeInOut(msg);
    }
}

