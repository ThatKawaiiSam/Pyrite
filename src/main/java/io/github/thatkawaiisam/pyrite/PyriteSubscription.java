package io.github.thatkawaiisam.pyrite;

import io.github.thatkawaiisam.pyrite.packet.Packet;
import io.github.thatkawaiisam.pyrite.packet.PacketContainer;
import io.github.thatkawaiisam.pyrite.packet.PacketListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PyriteSubscription extends JedisPubSub {

    private Pyrite pyrite;

    private Jedis resource;
    private Thread thread;

    /**
     * Pyrite Subscription.
     *
     * @param pyrite instance.
     */
    public PyriteSubscription(Pyrite pyrite) {
        this.pyrite = pyrite;
        this.resource = pyrite.getPool().getResource();
        this.pyrite.attemptAuth(this.resource);

        // Start Thread.
        this.thread = new Thread(() -> this.resource.psubscribe(this, "Pyrite:*"));
        this.thread.start();
    }

    /**
     * Cleanup this subscriber instance.
     */
    public void cleanup() {
        // Stop and cleanup thread (if created).
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.stop();
            this.thread = null;
        }

        // Unsubscribe from channels.
        if (isSubscribed()) {
            this.unsubscribe();
        }

        // Return resource to pool if still available.
        if (this.pyrite.getPool() != null && !this.pyrite.getPool().isClosed()) {
            this.pyrite.getPool().returnResource(this.resource);
        }
    }


    @Override
    public void onPMessage(String pattern, String channel, String message) {
        for (PacketContainer packetContainer : pyrite.getContainers()) {
            for (Method method : packetContainer.getClass().getDeclaredMethods()) {
                // Check if method has correct structure.
                if (!method.isAnnotationPresent(PacketListener.class)
                        || method.getParameters().length != 1
                        || !Packet.class.isAssignableFrom(method.getParameters()[0].getType())) {
                    continue;
                }

                // Check Channel (no channels acts as a catch all).
                if (method.getAnnotation(PacketListener.class).channels().length > 0 &&
                        !Arrays.asList(method.getAnnotation(PacketListener.class).channels()).contains(channel.replace("Pyrite:", ""))) {
                    continue;
                }

                // Invoke method with transformed data class.
                try {
                    Object object = packetContainer;
                    Class<?> type = method.getParameters()[0].getType();

                    // Check Packet Class Name.
                    Packet packet = (Packet) pyrite.getGson().fromJson(message, type);
                    if (!method.getParameters()[0].getClass().getName().equalsIgnoreCase(packet.getMetadata().getClassName())) {
                        continue;
                    }

                    // Set Additional Metadata & Invoke.
                    packet.getMetadata().setTimeReceived(System.currentTimeMillis());
                    method.invoke(object, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
