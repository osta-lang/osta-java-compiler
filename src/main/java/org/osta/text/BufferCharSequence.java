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
        int limit = buffer.limit();
        buffer.position(position + start);
        buffer.limit(position + end);
        BufferCharSequence result = slice();
        buffer.position(position);
        buffer.limit(limit);
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return asString();
    }

    public String asString() {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.mark();
        buffer.get(bytes);
        buffer.reset();
        return new String(bytes);
    }

    private BufferCharSequence slice() {
        return new BufferCharSequence(buffer.slice(buffer.position(), buffer.limit() - buffer.position()));
    }
}
