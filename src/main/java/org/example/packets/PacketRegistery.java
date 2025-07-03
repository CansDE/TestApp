package org.example.packets;

import org.example.DirectBuffer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketRegistery {

    private Map<Byte, Class<? extends Packet>> packetById = new HashMap<>();
    private Map<Class<? extends Packet>, Byte> idByPacket = new HashMap<>();

    private Map<Class<? extends Packet>, List<BiConsumer<SocketChannel, Packet>>> consumers = new HashMap<>();


    private DirectBuffer sendBuffer = new DirectBuffer(256);
    private DirectBuffer payload = new DirectBuffer(256);

    public PacketRegistery() {
        for(Class<? extends Packet> packetClazz : new Class[]{TestPacket.class, FischPacket.class}) {
            if(packetClazz.isAnnotationPresent(PacketInfo.class)) {
                byte id = packetClazz.getAnnotation(PacketInfo.class).id();
                packetById.put(id, packetClazz);
                idByPacket.put(packetClazz, id);
            }
        }
    }

    public void callPacket(SocketChannel socketChannel, DirectBuffer directBuffer) {
        byte id = directBuffer.readByte();
        int length = directBuffer.readInt();

        Class<? extends Packet> clazz = packetById.get(id);
        try {
            Packet packet = clazz.getConstructor().newInstance();
            packet.read(directBuffer);

            for (BiConsumer<SocketChannel, Packet> socketChannelPacketBiConsumer : consumers.get(clazz)) {
                socketChannelPacketBiConsumer.accept(socketChannel, packet);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendPacket(SocketChannel channel, Packet packet) {
        if(channel.isConnected()) {
            sendBuffer.writeByte(idByPacket.get(packet.getClass()));

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
    }

    public <T extends Packet> void handle(Class<T> clazz, BiConsumer<SocketChannel, T> biConsumer) {
        List<BiConsumer<SocketChannel, Packet>> con = consumers.getOrDefault(clazz, new ArrayList<>());
        con.add((BiConsumer<SocketChannel, Packet>) biConsumer);
        consumers.put(clazz, con);
    }

    public Class<?> packetById(Byte id) {
        return packetById.get(id);
    }

    public Byte idByPacket(Class<?> packet) {
        return idByPacket.get(packet);
    }

    public Map<Class<? extends Packet>, Byte> getIdByPacket() {
        return idByPacket;
    }

    public Map<Byte, Class<? extends Packet>> getPacketById() {
        return packetById;
    }
}
