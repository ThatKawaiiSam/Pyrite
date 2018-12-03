package io.github.thatkawaiisam.jedis.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class JedisSubscriber extends JedisPubSub {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private JedisHelper helper;
    private Jedis jedis;

    private IJedisSubscription subscription;

    public JedisSubscriber(JedisHelper helper, IJedisSubscription subscription) {
        this.helper = helper;
        this.subscription = subscription;
        this.jedis = new Jedis();

        helper.attemptAuth(this.jedis);

        new Thread(() -> {
            this.jedis.subscribe(this, subscription.subscriptionChannels());
        }).start();
    }

    public void cleanup() {
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
