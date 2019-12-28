package io.github.thatkawaiisam.jedis.helper;

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

    private IJedisSubscription subscription;
    private Thread subscriptionThread;

    public JedisSubscriber(String id, JedisHelper helper, IJedisSubscription subscription) {
        this.id = id;
        this.helper = helper;
        this.subscription = subscription;
        this.jedis = new Jedis();

        helper.attemptAuth(this.jedis);

        subscriptionThread = new Thread(() -> this.jedis.subscribe(this, subscription.subscriptionChannels()));
        subscriptionThread.start();
    }

    public void cleanup() {
        if (subscriptionThread != null && subscriptionThread.isAlive()) {
            subscriptionThread.stop();
        }
        this.unsubscribe();
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            JsonObject object = JSON_PARSER.parse(message).getAsJsonObject();
            String payload = object.get("payload").getAsString();
            JsonObject data = object.get("data").getAsJsonObject();
            this.subscription.handleMessage(payload, data);
        } catch (JsonParseException e) {
            //TODO better debug
            System.out.println("Received message that could not be parsed");
        }
    }
}
