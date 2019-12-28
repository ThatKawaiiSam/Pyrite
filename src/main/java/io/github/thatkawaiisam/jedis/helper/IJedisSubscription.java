package io.github.thatkawaiisam.jedis.helper;

import com.google.gson.JsonObject;

public interface IJedisSubscription {

    /**
     *
     * @param payload
     * @param data
     */
    void handleMessage(String payload, JsonObject data);

    /**
     *
     * @return
     */
    String[] subscriptionChannels();

}
