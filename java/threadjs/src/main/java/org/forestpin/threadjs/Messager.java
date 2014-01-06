package org.forestpin.threadjs;

import net.sf.json.JSONObject;

public class Messager extends ThreadJS {

    public void getRecord(JSONObject data, Callback cb) {
        System.out.println("[java] getRecord is called");
        cb.callback(null, data);

        JSONObject obj = JSONObject.fromObject("{vendor: \"pamona\", amount: 12313}");
        for (int i=0; i<10000; ++i) {
            send("onRecord", obj, null);
        }
    }
}
