package online.xloypaypa.dns.relay.dns;

import online.xloypaypa.dns.relay.dns.util.DnsMessageParser;
import online.xloypaypa.dns.relay.dns.util.NameParser;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.junit.Assert.*;

public class DNSMessageTest {

    private byte[] example;

    @Before
    public void setUp() throws Exception {
        this.example = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("dns-example")).readAllBytes();
    }

    @Test
    public void check_id() throws DNSParseException {
        DNSMessage dnsMessage = DnsMessageParser.parse(this.example);

        assertEquals(5868, dnsMessage.getId());
    }

    @Test
    public void check_flag() throws DNSParseException {
        DNSMessage dnsMessage = DnsMessageParser.parse(this.example);

        assertEquals(-32384, dnsMessage.getFlags());
    }

    @Test
    public void check_questions() throws DNSParseException {
        DNSMessage dnsMessage = DnsMessageParser.parse(this.example);

        DNSQuestion[] questions = dnsMessage.getQuestions();
        assertEquals(1, questions.length);
        assertEquals("www.baidu.com", questions[0].getName());
        assertEquals(1, questions[0].getQType());
        assertEquals(1, questions[0].getQClass());
    }

    @Test
    public void check_answers() throws DNSParseException {
        DNSMessage dnsMessage = DnsMessageParser.parse(this.example);

        DNSResourceRecord[] answers = dnsMessage.getAnswers();
        assertEquals(3, answers.length);

        assertEquals(1, answers[0].getRClass());
        assertEquals(5, answers[0].getRType());
        assertEquals(34, answers[0].getTtl());
        assertEquals("www.baidu.com", answers[0].getName());
        assertEquals("www.a.shifen.com", NameParser.parseName(ByteBuffer.wrap(answers[0].getRData())));

        assertEquals(1, answers[1].getRClass());
        assertEquals(1, answers[1].getRType());
        assertEquals(34, answers[1].getTtl());
        assertEquals("www.a.shifen.com", answers[1].getName());
        assertEquals(14, answers[1].getRData()[0] & 0xff);
        assertEquals(215, answers[1].getRData()[1] & 0xff);
        assertEquals(177, answers[1].getRData()[2] & 0xff);
        assertEquals(38, answers[1].getRData()[3] & 0xff);

        assertEquals(1, answers[2].getRClass());
        assertEquals(1, answers[2].getRType());
        assertEquals(34, answers[2].getTtl());
        assertEquals("www.a.shifen.com", answers[2].getName());
        assertEquals(14, answers[2].getRData()[0] & 0xff);
        assertEquals(215, answers[2].getRData()[1] & 0xff);
        assertEquals(177, answers[2].getRData()[2] & 0xff);
        assertEquals(39, answers[2].getRData()[3] & 0xff);
    }

    @Test
    public void check_to_bytes() throws DNSParseException {
        DNSMessage dnsMessage = DnsMessageParser.parse(this.example);

        byte[] result = DnsMessageParser.toBytes(dnsMessage);

        assertArrayEquals(this.example, result);
    }
}