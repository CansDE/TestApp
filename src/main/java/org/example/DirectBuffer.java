package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DirectBuffer {

    private ByteBuffer byteBuffer;
    public int writerIndex;
    public int readerIndex;

    public DirectBuffer(int capacity) {
        byteBuffer = ByteBuffer.allocateDirect(capacity);
    }

    public byte[] toByteArray() {
        byte[] out = new byte[readableBytes()];
        byteBuffer.position(readerIndex);
        byteBuffer.get(out, 0, out.length);
        return out;
    }
    public void checkCapacity(int bytesNeeded) {
        if(writeableBytes() < bytesNeeded) {
            int newCapacity = Math.max(size() * 2, writerIndex + bytesNeeded);
            ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(newCapacity);
            byteBuffer.position(0).limit(writerIndex);
            newByteBuffer.put(byteBuffer);
            byteBuffer = newByteBuffer;
            newByteBuffer = null;
        }
    }

    public byte readByte() {
        byte val = byteBuffer.get(readerIndex);
        readerIndex++;
        return val;
    }

    public void writeByte(byte val) {
        checkCapacity(1);
        byteBuffer.put(writerIndex, val);
        writerIndex++;
    }

    public void writeLong(long longs) {
        checkCapacity(7);
        byteBuffer.putLong(writerIndex, longs);
        writerIndex += 7;
    }

    public long readLong() {
        long val = byteBuffer.getLong(readerIndex);
        readerIndex += 7;
        return val;
    }

    public void writeBytes(byte[] bytes) {
        int aSize = bytes.length;
        writeInt(aSize);
        checkCapacity(aSize);
        byteBuffer.put(writerIndex, bytes);
        writerIndex += aSize;
    }

    public void writeDirectBuffer(DirectBuffer other) {
        int len = other.readableBytes();
        checkCapacity(len);
        ByteBuffer src = other.getRawBuffer();
        src.position(other.readerIndex).limit(other.writerIndex);
        byteBuffer.position(writerIndex);
        byteBuffer.put(src);
        writerIndex += len;
    }

    public byte[] readBytes() {
        int aSize = readInt();
        byte[] bytes = new byte[aSize];
        byteBuffer.get(readerIndex, bytes);
        readerIndex += aSize;
        return bytes;
    }
    public void writeString(String string) {
        writeBytes(string.getBytes());
    }

    public String readString() {
        byte[] bytes = readBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public void writeInt(int integer) {
        checkCapacity(4);
        byteBuffer.putInt(writerIndex, integer);
        writerIndex += 4;
    }
    public void writeDouble(double value) {
        checkCapacity(8);
        byteBuffer.putDouble(writerIndex, value);
        writerIndex += 8;
    }
    public void writeFloat(float value) {
        checkCapacity(4);
        byteBuffer.putFloat(writerIndex, value);
        writerIndex += 4;
    }
    public int readInt() {
        int val = byteBuffer.getInt(readerIndex);
        readerIndex += 4;
        return val;
    }
    public double readDouble() {
        double val = byteBuffer.getDouble(readerIndex);
        readerIndex += 8;
        return val;
    }
    public float readFloat() {
        float val = byteBuffer.getFloat(readerIndex);
        readerIndex += 4;
        return val;
    }


    public void clear() {
        readerIndex = 0;
        writerIndex = 0;
        byteBuffer.clear();
    }

    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    public int writeableBytes() {
        return byteBuffer.capacity() - writerIndex;
    }

    public int size() {
        return byteBuffer.capacity();
    }


    public ByteBuffer getRawBuffer() {
        return byteBuffer;
    }
}
