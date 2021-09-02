package io.github.thatkawaiisam.pyrite.packet;

import lombok.Data;

@Data
public class PacketMetadata {

    private String className;
    private long timeSent = System.currentTimeMillis();

    public PacketMetadata(Packet packet) {
        this.className = packet.getClass().getName();
    }

}
