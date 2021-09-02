package io.github.thatkawaiisam.pyrite;

import io.github.thatkawaiisam.pyrite.packet.PyritePacket;
import io.github.thatkawaiisam.pyrite.packet.PyritePacketContainer;
import io.github.thatkawaiisam.pyrite.packet.PyritePacketListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PyriteSubscription extends JedisPubSub {

    private Pyrite pyrite;

    private Jedis subscriptionResource;
    private Thread subscriptionThread;

    /**
     * Pyrite Subscription.
     *
     * @param pyrite instance.
     */
    public PyriteSubscription(Pyrite pyrite) {
        this.pyrite = pyrite;
        this.subscriptionResource = pyrite.getPool().getResource();
        this.pyrite.attemptAuth(subscriptionResource);

        // Start Thread.
        this.subscriptionThread = new Thread(() -> this.subscriptionResource.psubscribe(this, "Pyrite:*"));
        this.subscriptionThread.start();
    }

    /**
     * Cleanup this subscriber instance.
     */
    public void cleanup() {
        // Stop and cleanup thread (if created).
        if (subscriptionThread != null && subscriptionThread.isAlive()) {
            subscriptionThread.stop();
            subscriptionThread = null;
        }

        // Unsubscribe from channels.
        if (isSubscribed()) {
            this.unsubscribe();
        }
        
        // Return resource to pool if still available.
        if (this.pyrite.getPool() != null && !this.pyrite.getPool().isClosed()) {
            this.pyrite.getPool().returnResource(subscriptionResource);
        }
    }


    @Override
    public void onPMessage(String pattern, String channel, String message) {
        for (PyritePacketContainer packetContainer : pyrite.getContainers()) {
            for (Method method : packetContainer.getClass().getDeclaredMethods()) {
                // Check if method has correct structure.
                if (!method.isAnnotationPresent(PyritePacketListener.class)
                        || method.getParameters().length != 1
                        || !PyritePacket.class.isAssignableFrom(method.getParameters()[0].getType())) {
                    continue;
                }

                // Check Channel (no channels acts as a catch all).
                if (method.getAnnotation(PyritePacketListener.class).channels().length > 0 &&
                        !Arrays.asList(method.getAnnotation(PyritePacketListener.class).channels()).contains(channel)) {
                    continue;
                }

                // Invoke method with transformed data class.
                try {
                    Object object = packetContainer.getClass().newInstance();
                    Class<?> type = method.getParameters()[0].getType();
                    method.invoke(object, pyrite.getGson().fromJson(message, type));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
