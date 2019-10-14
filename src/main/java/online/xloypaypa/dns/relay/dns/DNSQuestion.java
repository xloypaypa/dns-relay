package online.xloypaypa.dns.relay.dns;

import online.xloypaypa.dns.relay.dns.util.ByteAble;
import online.xloypaypa.dns.relay.dns.util.NameParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A single question in a DNS message
 * 
 * @author Alexey Borzenkov
 * 
 */
public final class DNSQuestion implements ByteAble {
	private final String name;
	private final short qtype;
	private final short qclass;

	public DNSQuestion(String name, short qtype, short qclass) {
		this.name = name;
		this.qtype = qtype;
		this.qclass = qclass;
	}

	public final String getName() {
		return name;
	}

	public final int getQType() {
		return qtype;
	}

	public final int getQClass() {
		return qclass;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof DNSQuestion) {
			final DNSQuestion other = (DNSQuestion) obj;
			return name.equals(other.name) && qtype == other.qtype
					&& qclass == other.qclass;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (name.hashCode() * 31 + qtype) * 31 + qclass;
	}

	@Override
	public byte[] toBytes() {
		byte[] name = NameParser.toBytes(this.name);
		ByteBuffer byteBuffer = ByteBuffer.allocate(name.length + 4);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		byteBuffer.put(name);
		byteBuffer.putShort(this.qtype);
		byteBuffer.putShort(this.qclass);
		return byteBuffer.array();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Question { name: ");
		builder.append(name);
		builder.append(", qtype: ");
		builder.append(qtype);
		builder.append(", qclass: ");
		builder.append(qclass);
		builder.append(" }");
		return builder.toString();
	}
}
