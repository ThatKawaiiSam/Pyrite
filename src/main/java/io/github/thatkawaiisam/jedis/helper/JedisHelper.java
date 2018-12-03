package io.github.thatkawaiisam.jedis.helper;

import com.google.gson.JsonObject;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;

@Getter
public class JedisHelper {

    private JedisPool pool;
    private JedisCredentials credentials;
    private JedisPublisher publisher;
    private Set<JedisSubscriber> subscribers = new HashSet<>();

    public JedisHelper(JedisCredentials credentials) {
        this.credentials = credentials;
        this.pool = new JedisPool(this.getCredentials().getAddress(), this.credentials.getPort());

        try (Jedis jedis = this.pool.getResource()) {
            attemptAuth(jedis);

            this.publisher = new JedisPublisher(this);
        }
    }

    public void close() {
        for (JedisSubscriber subscriber : subscribers) {
            if (subscriber.isSubscribed()) {
                subscriber.unsubscribe();
            }
        }
        this.pool.close();
    }

    public boolean isActive() {
        return this.pool != null && !this.pool.isClosed();
    }

    public void attemptAuth(Jedis jedis) {
        if (this.credentials.isAuth()) {
            jedis.auth(this.credentials.getPassword());
        }
    }

    public void write(Enum payloadID, JsonObject data, String channel) {
        JsonObject object = new JsonObject();

        object.addProperty("payload", payloadID.name());
        object.add("data", data == null ? new JsonObject() : data);

        publisher.write(channel, object);
    }

    public <T> T runCommand(IRedisCommand<T> redisCommand) {
        Jedis jedis = this.pool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();

            if (jedis != null) {
                this.pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                this.pool.returnResource(jedis);
            }
        }

        return result;
    }
}
