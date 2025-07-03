package org.example.packets;

import org.example.DirectBuffer;

@PacketInfo(id = 1)
public class TestPacket extends Packet {

    private long time;

    public TestPacket(){}

    public TestPacket(long time) {
        this.time = time;
    }

    @Override
    public void write(DirectBuffer directBuffer) {
        directBuffer.writeLong(time);
    }

    @Override
    public void read(DirectBuffer directBuffer) {
        time = directBuffer.readLong();
    }

    public long getTime() {
        return time;
    }
}
