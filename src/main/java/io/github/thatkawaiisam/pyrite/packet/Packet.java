package io.github.thatkawaiisam.pyrite.packet;

import lombok.Data;

/**
 * Packet
 */
@Data
public class Packet {

    private PacketMetadata metadata = new PacketMetadata(this);

}