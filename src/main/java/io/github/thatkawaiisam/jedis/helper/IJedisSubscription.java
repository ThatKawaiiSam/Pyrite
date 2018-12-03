package io.github.thatkawaiisam.jedis.helper;

import com.google.gson.JsonObject;

public interface IJedisSubscription {

    void handleMessage(String payload, JsonObject data);

    String[] subscriptionChannels();

}
