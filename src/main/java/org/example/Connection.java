package org.example;

import org.example.packets.Packet;
import org.example.packets.PacketRegistery;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection {

    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private PacketRegistery packetRegistery;
    private DirectBuffer readerBuffer;

    private DirectBuffer sendBuffer = new DirectBuffer(256);
    private DirectBuffer payload = new DirectBuffer(256);

    private Queue<ByteBuffer> sendingQueue = new ConcurrentLinkedQueue<>();
    private boolean writePending;

    public Connection(SocketChannel socketChannel, SelectionKey selectionKey, PacketRegistery packetRegistery) {
        this.socketChannel = socketChannel;
        this.selectionKey = selectionKey;
        this.readerBuffer = new DirectBuffer(256);
        this.packetRegistery = packetRegistery;
    }

    public void sendPacket(Packet packet) {
        if(socketChannel.isConnected()) {
            sendBuffer.writeByte(packetRegistery.getIdByPacket().get(packet.getClass()));

            packet.write(payload);

            sendBuffer.writeInt(payload.readableBytes());
            sendBuffer.writeDirectBuffer(payload);

            ByteBuffer buf = sendBuffer.getRawBuffer();
            buf.position(sendBuffer.readerIndex);
            buf.limit(sendBuffer.writerIndex);

            sendingQueue.add(buf);
            if(!writePending) {
                writePending = true;
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();
            }

            sendBuffer.clear();
            payload.clear();

        }

    }
    public void doWrite() {
        while(!sendingQueue.isEmpty()) {
            ByteBuffer packet = sendingQueue.peek();
            try {
                socketChannel.write(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(packet.hasRemaining()) return;
            sendingQueue.poll();
        }
        writePending = false;
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
    }
    public void doRead() throws IOException {
            try {
                int read = readerBuffer.read(socketChannel);
                if (read == -1) {
                    System.out.println("Client disconnected (Read = -1)");
                    socketChannel.close();
                    return;
                }
                while(true) {
                    if(readerBuffer.readableBytes() < 5) return;

                    byte id = readerBuffer.peekByte(0);
                    int payload = readerBuffer.peekInt(1);

                    if(readerBuffer.readableBytes() < 5+payload) return;

                    readerBuffer.skipBytes(5);
                    packetRegistery.callPacket(socketChannel, readerBuffer, id);
                    readerBuffer.clear();
                }
            }catch (IOException ex) {
                socketChannel.close();
                System.out.println("Client disconnected (IOException)");
            }
    }
}
