package online.xloypaypa.dns.relay.dns.util;

import online.xloypaypa.dns.relay.dns.DNSMessage;
import online.xloypaypa.dns.relay.dns.DNSParseException;
import online.xloypaypa.dns.relay.dns.DNSQuestion;
import online.xloypaypa.dns.relay.dns.DNSResourceRecord;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DnsMessageParser {

    public static byte[] toBytes(DNSMessage dnsMessage) {
        int length = 0;
        byte[] header = buildHeader(dnsMessage);
        length += header.length;

        byte[] questions = buildByteAbleArray(dnsMessage.getQuestions());
        length += questions.length;

        byte[] answers = buildByteAbleArray(dnsMessage.getAnswers());
        length+= answers.length;

        byte[] nameServers = buildByteAbleArray(dnsMessage.getNameServers());
        length+= nameServers.length;

        byte[] additionalRecords = buildByteAbleArray(dnsMessage.getAdditionalRecords());
        length+= additionalRecords.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.put(header);
        byteBuffer.put(questions);
        byteBuffer.put(answers);
        byteBuffer.put(nameServers);
        byteBuffer.put(additionalRecords);
        return byteBuffer.array();
    }

    private static byte[] buildHeader(DNSMessage dnsMessage) {
        ByteBuffer header = ByteBuffer.allocate(12);
        header.order(ByteOrder.BIG_ENDIAN);
        header.putShort(dnsMessage.getId());
        header.putShort(dnsMessage.getFlags());
        header.putShort((short) dnsMessage.getQuestions().length);
        header.putShort((short) dnsMessage.getAnswers().length);
        header.putShort((short) dnsMessage.getNameServers().length);
        header.putShort((short) dnsMessage.getAdditionalRecords().length);
        return header.array();
    }

    private static byte[] buildByteAbleArray(ByteAble[] dnsQuestions) {
        int length = 0;
        byte[][] questions = new byte[dnsQuestions.length][];
        for (int i = 0; i < dnsQuestions.length; i++) {
            questions[i] = dnsQuestions[i].toBytes();
            length += questions[i].length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        for (int i = 0; i < dnsQuestions.length; i++) {
            byteBuffer.put(questions[i]);
        }
        return byteBuffer.array();
    }

    public static DNSMessage parse(byte[] bytes)
            throws DNSParseException {
        return parse(ByteBuffer.wrap(bytes));
    }

    private static DNSMessage parse(ByteBuffer buffer)
            throws DNSParseException {
        assert buffer.order() == ByteOrder.BIG_ENDIAN;
        final short id = buffer.getShort();
        final short flags = buffer.getShort();
        final short qdcount = buffer.getShort();
        final short ancount = buffer.getShort();
        final short nscount = buffer.getShort();
        final short arcount = buffer.getShort();
        DNSQuestion[] questions = new DNSQuestion[qdcount];
        DNSResourceRecord[] answers = new DNSResourceRecord[ancount];
        DNSResourceRecord[] nameservers = new DNSResourceRecord[nscount];
        DNSResourceRecord[] additionalrecords = new DNSResourceRecord[arcount];
        if (qdcount > 256)
            throw new DNSParseException("Too many questions");
        if (ancount > 256)
            throw new DNSParseException("Too many answers");
        if (nscount > 256)
            throw new DNSParseException("Too many nameserver records");
        if (arcount > 256)
            throw new DNSParseException("Too many additional records");
        for (int i = 0; i < qdcount; ++i) {
            questions[i] = parseQuestion(buffer);
        }
        try {
            for (int i = 0; i < ancount; ++i) {
                answers[i] = parseResourceRecord(buffer);
            }
            for (int i = 0; i < nscount; ++i) {
                nameservers[i] = parseResourceRecord(buffer);
            }
            for (int i = 0; i < arcount; ++i) {
                additionalrecords[i] = parseResourceRecord(buffer);
            }
        } catch (BufferUnderflowException e) {
            // Failure to read answers is not fatal in our case
            e.printStackTrace();
        }
        return new DNSMessage(id, flags, questions, answers, nameservers, additionalrecords);
    }

    private static DNSQuestion parseQuestion(ByteBuffer buffer)
            throws DNSParseException {
        final String name = NameParser.parseName(buffer);
        final short qtype = buffer.getShort();
        final short qclass = buffer.getShort();
        return new DNSQuestion(name, qtype, qclass);
    }

    private static DNSResourceRecord parseResourceRecord(ByteBuffer buffer)
            throws DNSParseException {
        final String name = NameParser.parseName(buffer);
        final short rtype = buffer.getShort();
        final short rclass = buffer.getShort();
        final int ttl = buffer.getInt();
        final int rdlength = buffer.getShort() & 0xffff;
        final byte[] rdata = new byte[rdlength];
        buffer.get(rdata);
        return new DNSResourceRecord(name, rtype, rclass, ttl, rdata);
    }

}
