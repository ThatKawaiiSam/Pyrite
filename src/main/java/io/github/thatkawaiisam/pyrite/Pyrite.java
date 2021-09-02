package io.github.thatkawaiisam.pyrite;

import lombok.Getter;
import com.google.gson.Gson;
import io.github.thatkawaiisam.pyrite.packet.PyritePacket;
import io.github.thatkawaiisam.pyrite.packet.PyritePacketContainer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Pyrite {

    private PyriteCredentials credentials;
    private PyriteSubscription subscription;

    private Gson gson = new Gson();
    private JedisPool pool;

    private List<PyritePacketContainer> containers = new ArrayList<>();

    /**
     * Pyrite.
     *
     * @param credentials to connection to Redis with.
     */
    public Pyrite(PyriteCredentials credentials) {
        this.credentials = credentials;
        this.pool = new JedisPool(this.getCredentials().getAddress(), this.credentials.getPort());
        this.subscription = new PyriteSubscription(this);
    }

    /**
     * Register Packet Container.
     *
     * @param packetContainer to register.
     */
    public void registerContainer(PyritePacketContainer packetContainer) {
        this.containers.add(packetContainer);
    }

    /**
     * Unregister Packet Container.
     *
     * @param packetContainer to unregister.
     */
    public void unregisterContainer(PyritePacketContainer packetContainer) {
        this.containers.remove(packetContainer);
    }

    /**
     * Run Redis Command.
     *
     * @param command to be run.
     * @return redis resource to push back to the pool.
     */
    public <T> T runRedisCommand(RedisCommand<T> command) {
        Jedis jedis = this.pool.getResource();
        T result = null;

        try {
            attemptAuth(jedis);
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

    /**
     * Close Pyrite instance.
     */
    public void close() {
        // Cleanup Subscription.
        this.getSubscription().cleanup();

        // Unregister containers.
        this.containers.forEach(this::unregisterContainer);

        // Close Jedis Pool.
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
     * Send Pyrite Packet to PubSub.
     *
     * @param packet to send.
     */
    public void sendPacket(PyritePacket packet, String channel) {
        if (!isActive()) {
            try {
                throw new Exception("Unable to send packets while pool is inactive.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        runRedisCommand(redis -> {
            attemptAuth(redis);
            redis.publish("Pyrite:" + channel, gson.toJson(packet));
            return redis;
        });
    }

}
