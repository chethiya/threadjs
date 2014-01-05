package org.forestpin.threadjs;

import net.sf.json.JSONObject;

public interface Callback {
    public final Callback EMPTY = new Callback() {
        public void callback(String err, JSONObject data) {

        }
    };
    public void callback(String err, JSONObject data);
}
