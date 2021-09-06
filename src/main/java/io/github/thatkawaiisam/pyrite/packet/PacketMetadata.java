package io.github.thatkawaiisam.pyrite.packet;

import lombok.Data;

@Data
public class PacketMetadata {

    private String className;
    private long timeSent = System.currentTimeMillis();
    private long timeReceived = System.currentTimeMillis();

    /**
     * Packet Metadata.
     *
     * @param packet which metadata belongs to.
     */
    public PacketMetadata(Packet packet) {
        this.className = packet.getClass().getName();
    }

}
