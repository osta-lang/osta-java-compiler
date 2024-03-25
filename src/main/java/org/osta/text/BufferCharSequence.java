package org.osta.text;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BufferCharSequence implements CharSequence {

    private final ByteBuffer buffer;

    public BufferCharSequence(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int length() {
        return buffer.remaining();
    }

    @Override
    public char charAt(int index) {
        return (char) buffer.get(buffer.position() + index);
    }

    @Override
    @NotNull
    public CharSequence subSequence(int start, int end) {
        // Return as string
        int position = buffer.position();
        byte[] bytes = new byte[end - start];
        buffer.get(bytes);
        buffer.position(position);
        return new String(bytes);
    }

    @Override
    @NotNull
    public String toString() {
        return super.toString();
    }
}
