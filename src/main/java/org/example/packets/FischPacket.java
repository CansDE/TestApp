package org.example.packets;

import org.example.DirectBuffer;

@PacketInfo(id = 2)
public class FischPacket extends Packet{

    private String string;
    public FischPacket(){}

    public FischPacket(String string) {
        this.string = string;
    }

    @Override
    public void write(DirectBuffer directBuffer) {
        directBuffer.writeString(string);
    }

    @Override
    public void read(DirectBuffer directBuffer) {
        string = directBuffer.readString();
    }

    public String getString() {
        return string;
    }
}
