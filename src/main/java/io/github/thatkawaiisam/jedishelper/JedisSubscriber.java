package io.github.thatkawaiisam.jedishelper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@Getter
public class JedisSubscriber extends JedisPubSub {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private JedisHelper helper;
    private Jedis jedis;

    private String id;

    private JedisSubscription subscription;
    private Thread subscriptionThread;

    /**
     * Jedis Subscriber.
     *
     * @param id of this subscriber object.
     * @param helper instance.
     * @param subscription interface that is being
     */
    public JedisSubscriber(String id, JedisHelper helper, JedisSubscription subscription, boolean async) {
        this.id = id;
        this.helper = helper;
        this.subscription = subscription;
        this.jedis = new Jedis();

        helper.attemptAuth(this.jedis);

        if (async) {
            subscriptionThread = new Thread(() -> this.jedis.subscribe(this, subscription.subscriptionChannels()));
            subscriptionThread.start();
        } else {
            this.jedis.subscribe(this, subscription.subscriptionChannels());
        }
    }

    /**
     * Cleanup this subscriber instance.
     */
    public void cleanup() {
        if (subscriptionThread != null && subscriptionThread.isAlive()) {
            subscriptionThread.stop();
            subscriptionThread = null;
        }
        if (isSubscribed()) {
            this.unsubscribe();
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            JsonObject object = JSON_PARSER.parse(message).getAsJsonObject();
            String payload = object.get("payload").getAsString();
            JsonObject data = object.get("data").getAsJsonObject();
            this.subscription.handleMessage(payload, data);
        } catch (JsonParseException e) {
            System.out.println("Received message that could not be parsed. (Channel: " + channel + ")");
            e.printStackTrace();
        }
    }
}
