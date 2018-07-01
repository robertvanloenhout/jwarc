/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2018 National Library of Australia and the jwarc contributors
 */

package org.netpreserve.jwarc.lowlevel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.US_ASCII;

public final class HeaderField {
    private final byte[] bytes;
    private int hashCode;

    HeaderField(ByteBuffer buffer) {
        bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
    }

    HeaderField(String name) {
        this.bytes = name.getBytes(US_ASCII);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderField that = (HeaderField) o;
        if (bytes.length != that.bytes.length) return false;
        if (hashCode != 0 && that.hashCode != 0 && hashCode != that.hashCode()) return false;
        for (int i = 0; i < bytes.length; i++) {
            if (asciiLower(bytes[i]) != asciiLower(that.bytes[i])) {
                return false;
            }
        }
        return true;
    }

    private byte asciiLower(byte b) {
        return (byte)(b >= 'A' && b <= 'Z' ? b | ' ' : b);
    }


    // hopefully not too bad case-insensitive hash
    // based on Yann Collet's xxhash32 but only the short string path
    // we mask out the ASCII case bit
    @Override
    public int hashCode() {
        if (hashCode != 0) return hashCode;

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
        int x = 374761393 + bytes.length;

        while (buffer.remaining() >= 4) {
            x += (buffer.getInt() & ~0x20202020) * 0xc2b2ae3d;
            x = Integer.rotateLeft(x, 17) * 668265263;
        }

        while (buffer.hasRemaining()) {
            x += (buffer.get() & 0xdf) * 374761393;
            x = Integer.rotateLeft(x, 11) * 0x9e3779b1;
        }

        x ^= x >>> 15;
        x *= 0x85ebca77;
        x ^= x >>> 13;
        x *= 0xc2b2ae3d;
        x ^= x >>> 16;

        return hashCode = x;
    }

    @Override
    public String toString() {
        return new String(bytes, US_ASCII);
    }


}
