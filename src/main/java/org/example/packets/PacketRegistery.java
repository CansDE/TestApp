package org.example.packets;

import org.example.DirectBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistery {

    private Map<Byte, Class<?>> packetById = new HashMap<>();
    private Map<Class<?>, Byte> idByPacket = new HashMap<>();

    private DirectBuffer sendBuffer = new DirectBuffer(256);
    private DirectBuffer payload = new DirectBuffer(256);

    public PacketRegistery() {
        packetById.put((byte) 0x01, TestPacket.class);
        idByPacket.put(TestPacket.class, (byte)0x01);
    }

    public void sendPacket(SocketChannel channel, Packet packet) {
        sendBuffer.writeByte(packet.packetID());

        packet.write(payload);

        sendBuffer.writeInt(payload.readableBytes());
        sendBuffer.writeDirectBuffer(payload);

        ByteBuffer buf = sendBuffer.getRawBuffer();
        buf.position(sendBuffer.readerIndex);
        buf.limit(sendBuffer.writerIndex);


        try {
            channel.write(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendBuffer.clear();
        payload.clear();
    }

    public Class<?> packetById(Byte id) {
        return packetById.get(id);
    }

    public Byte idByPacket(Class<?> packet) {
        return idByPacket.get(packet);
    }

    public Map<Class<?>, Byte> getIdByPacket() {
        return idByPacket;
    }

    public Map<Byte, Class<?>> getPacketById() {
        return packetById;
    }
}
