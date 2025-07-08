package org.example.packets;

import org.example.DirectBuffer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketRegistery {

    private Map<Byte, Constructor<? extends Packet>> packetById = new HashMap<>();
    private Map<Class<? extends Packet>, Byte> idByPacket = new HashMap<>();

    private Map<Class<? extends Packet>, List<BiConsumer<SocketChannel, Packet>>> consumers = new HashMap<>();

    private DirectBuffer sendBuffer = new DirectBuffer(256);
    private DirectBuffer payload = new DirectBuffer(256);

    public PacketRegistery() {
        for(Class<? extends Packet> packetClazz : new Class[]{TestPacket.class, FischPacket.class}) {
            if(packetClazz.isAnnotationPresent(PacketInfo.class)) {
                byte id = packetClazz.getAnnotation(PacketInfo.class).id();
                try {
                    packetById.put(id, packetClazz.getConstructor());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                idByPacket.put(packetClazz, id);
            }
        }
    }

    public void callPacket(SocketChannel socketChannel, DirectBuffer directBuffer, byte id) {
        try {
            Packet packet = packetById.get(id).newInstance();
            packet.read(directBuffer);
            List<BiConsumer<SocketChannel, Packet>> con = consumers.get(packet.getClass());
            for (BiConsumer<SocketChannel, Packet> socketChannelPacketBiConsumer : con) {
                socketChannelPacketBiConsumer.accept(socketChannel, packet);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    public <T extends Packet> void handle(Class<T> clazz, BiConsumer<SocketChannel, T> biConsumer) {
        List<BiConsumer<SocketChannel, Packet>> con = consumers.getOrDefault(clazz, new ArrayList<>());
        con.add((BiConsumer<SocketChannel, Packet>) biConsumer);
        consumers.put(clazz, con);
    }



    public Byte idByPacket(Class<?> packet) {
        return idByPacket.get(packet);
    }

    public Map<Class<? extends Packet>, Byte> getIdByPacket() {
        return idByPacket;
    }

}
