package org.example.packets;

import org.example.DirectBuffer;

public abstract class Packet {

    public abstract void write(DirectBuffer directBuffer);
    public abstract void read(DirectBuffer directBuffer);

}
