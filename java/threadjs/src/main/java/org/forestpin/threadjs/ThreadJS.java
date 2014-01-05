package org.forestpin.threadjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadJS {
    private String host = "localhost";
    private int port = 11010;
    private PrintWriter out;
    private BufferedReader in;
    private final String _START = "[START]";
    private final String _END = "[END]";

    private char callback = 'x';
    private String reqId = null;
    private String method = null;
    private String msg = null;

    ThreadJS() {
        init();
    }

    ThreadJS(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void resetParams() {
        callback = 'x';
        reqId = null;
        method = null;
        msg = null;
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

    private void printParams() {
        System.out.println(callback);
        System.out.println(reqId);
        System.out.println(method);
        System.out.println(msg);
    }

    private void onMessage(boolean callback, String reqId, String method, String msg) {
        // TODO call the callback obj in case of callback. If not send call the
        // method!
        System.out.println("Messsage received");
        printParams();

        out.println(_START + "1" + reqId + "\n{\"data\": {}}" + _END);
    }

    private void init() {
        Socket socket;
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
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String str;

            // TODO using a hacky algorithm to decode message. Using new line
            // characters assuming there are new line character at the end of
            // each message
            resetParams();
            boolean started = false;
            while ((str = in.readLine()) != null) {
                System.out.println("Read: " + str);
                if (isNewMessage(str)) {
                    resetParams();
                    str = str.substring(_START.length());
                    started = true;
                }
                if (!started)
                    continue;

                if (method != null || (callback == '1' && reqId != null)) {
                    if (msg == null)
                        msg = "";
                    if (isEndMessage(str)) {
                        str = str.substring(0, str.length() - _END.length());
                        msg += str;
                        onMessage((callback == '1' ? true : false), reqId, method, msg);
                        started = false;
                        resetParams();
                    } else
                        msg += str;

                } else if (reqId != null) {
                    method = str;
                } else {
                    if (str.length() < 2) {
                        started = false;
                        resetParams();
                    } else {
                        callback = str.charAt(0);
                        reqId = str.substring(1);
                    }
                }
                printParams();
            }
            System.out.println("Nodejs has closed the socket");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}