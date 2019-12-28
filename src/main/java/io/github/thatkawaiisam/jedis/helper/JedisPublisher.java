package io.github.thatkawaiisam.jedis.helper;

import com.google.gson.JsonObject;

public class JedisPublisher {

    private JedisHelper helper;

    public JedisPublisher(JedisHelper helper) {
        this.helper = helper;
    }

    protected void write(String channelID, JsonObject payload) {
        if (!helper.isActive()) {
            System.out.println("Unable to publish message while pool is inactive.");
            return;
        }

        helper.runCommand(redis -> {
            helper.attemptAuth(redis);
            redis.publish(channelID, payload.toString());
            return redis;
        });
    }
}
