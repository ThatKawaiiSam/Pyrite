package io.github.thatkawaiisam.jedishelper;

import com.google.gson.JsonObject;

public interface JedisSubscription {

    /**
     * Incoming message being handled.
     *
     * @param payload identifier.
     * @param data of message in JSON form.
     */
    void handleMessage(String payload, JsonObject data);

    /**
     * Subscription Channels of Subscription Instance.
     */
    String[] subscriptionChannels();

}
