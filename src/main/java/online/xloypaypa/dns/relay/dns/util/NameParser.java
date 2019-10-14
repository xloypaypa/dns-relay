package online.xloypaypa.dns.relay.dns.util;

import online.xloypaypa.dns.relay.dns.DNSParseException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

public final class NameParser {

    private NameParser() {
    }

    public static byte[] toBytes(String domain) {
        byte[] domainBytes = domain.getBytes();
        ByteBuffer name = ByteBuffer.allocate(domainBytes.length + 2);
        name.order(ByteOrder.BIG_ENDIAN);
        name.put((byte) 0);
        byte count = 0;
        for (int i = domainBytes.length - 1; i >= 0; i--) {
            if (domainBytes[i] != '.') {
                name.put(domainBytes[i]);
                count++;
            } else {
                name.put(count);
                count = 0;
            }
        }
        name.put(count);
        byte[] result = name.array();
        for (int i = 0; i < result.length / 2; i++) {
            byte temp = result[i];
            result[i] = result[result.length - 1 - i];
            result[result.length - 1 - i] = temp;
        }
        return result;
    }

    public static String parseName(ByteBuffer buffer)
            throws DNSParseException {
        int jumps = 0;
        int lastpos = -1;
        CharBuffer dst = CharBuffer.allocate(255);
        while (true) {
            final byte b = buffer.get();
            if (b == 0)
                break;
            switch (b & 0xC0) {
                case 0x00:
                    // length that follows
                    if (dst.position() != 0) {
                        if (dst.remaining() < b + 1) {
                            throw new DNSParseException("DNS name too long");
                        }
                        dst.put('.');
                    } else if (dst.remaining() < b) {
                        throw new DNSParseException("DNS name too long");
                    }
                    for (int i = 0; i < b; ++i) {
                        dst.put((char) (buffer.get() & 0xff));
                    }
                    break;
                case 0xc0:
                    // offset of the new position
                    if (++jumps >= 16)
                        throw new DNSParseException("Too many DNS name jumps");
                    final int offset = ((b & 0x3f) << 8) | (buffer.get() & 0xff);
                    if (lastpos == -1)
                        lastpos = buffer.position();
                    buffer.position(offset);
                    break;
                default:
                    throw new DNSParseException("Unsupported DNS name byte");
            }
        }
        if (lastpos != -1)
            buffer.position(lastpos);
        return dst.flip().toString();
    }

}
