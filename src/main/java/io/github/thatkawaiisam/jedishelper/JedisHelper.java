package io.github.thatkawaiisam.jedishelper;

import com.google.gson.Gson;
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

    private Gson gson = new Gson();

    /**
     * Jedis Helper.
     *
     * @param credentials for Jedis instance.
     */
    public JedisHelper(JedisCredentials credentials) {
        this.credentials = credentials;
        this.pool = new JedisPool(this.getCredentials().getAddress(), this.credentials.getPort());

        try (Jedis jedis = this.pool.getResource()) {
            attemptAuth(jedis);
            this.publisher = new JedisPublisher(this);
        }
    }

    /**
     * Close helper instance.
     */
    public void close() {
        subscribers.forEach(JedisSubscriber::cleanup);
        if (!this.pool.isClosed()) {
            this.pool.close();
        }
    }

    /**
     * Check whether the connection to the pool is currently active or closed.
     *
     * @return whether connection is active or not.
     */
    public boolean isActive() {
        return this.pool != null && !this.pool.isClosed();
    }

    /**
     * Attempt to authenticate Jedis instance (if needed) from current credentials.
     *
     * @param jedis instance.
     */
    public void attemptAuth(Jedis jedis) {
        if (this.credentials.isAuth()) {
            jedis.auth(this.credentials.getPassword());
        }
    }

    /**
     * Write message to Jedis pub sub.
     *
     * @param id of payload.
     * @param data to be sent in the form of JSON.
     * @param channel to be sent to.
     */
    public void write(Enum id, JsonObject data, String channel) {
        JsonObject object = new JsonObject();

        object.addProperty("payload", id.name());
        object.add("data", data == null ? new JsonObject() : data);

        try {
            publisher.write(channel, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run Redis Command.
     *
     * @param command to be run.
     * @return redis resource to push back to the pool.
     */
    public <T> T runCommand(RedisCommand<T> command) {
        Jedis jedis = this.pool.getResource();
        T result = null;

        try {
            result = command.execute(jedis);
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
