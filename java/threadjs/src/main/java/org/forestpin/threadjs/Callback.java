package org.forestpin.threadjs;

import net.sf.json.JSONObject;

public interface Callback {
    public void callback(String err, JSONObject data);
}
