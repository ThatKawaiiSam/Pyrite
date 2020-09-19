package io.github.thatkawaiisam.jedishelper;

import com.google.gson.JsonObject;

public class JedisPublisher {

    private JedisHelper helper;

    /**
     * Jedis Publisher
     *
     * @param helper instance.
     */
    public JedisPublisher(JedisHelper helper) {
        this.helper = helper;
    }

    /**
     * Write JSON Payload to Redis channel through pool.
     *
     * @param id of channel that message is being sent to.
     * @param payload that is being sent.
     */
    protected void write(String id, JsonObject payload) throws Exception {
        if (!helper.isActive()) {
            throw new Exception("Unable to publish messages while pool is inactive.");
        }

        helper.runCommand(redis -> {
            helper.attemptAuth(redis);
            redis.publish(id, payload.toString());
            return redis;
        });
    }
}
