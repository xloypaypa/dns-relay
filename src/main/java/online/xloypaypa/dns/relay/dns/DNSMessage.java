package online.xloypaypa.dns.relay.dns;

/**
 * Parsed DNS message
 * 
 * @author Alexey Borzenkov
 *
 */
public final class DNSMessage {
	private static final short RESPONSE_MASK = (short) 0x8000;
	private static final short OPCODE_MASK = (short) 0x7800;
	private static final int OPCODE_SHIFT = 11;
	private static final short AA_MASK = (short) 0x0400;
	private static final short TC_MASK = (short) 0x0200;
	private static final short RD_MASK = (short) 0x0100;
	private static final short RA_MASK = (short) 0x0080;
	private static final short RCODE_MASK = (short) 0x000f;

	private final short id;
	private final short flags;
	private final DNSQuestion[] questions;
	private final DNSResourceRecord[] answers;
	private final DNSResourceRecord[] nameServers;
	private final DNSResourceRecord[] additionalRecords;

	public DNSMessage(short id, short flags, DNSQuestion[] questions, DNSResourceRecord[] answers, DNSResourceRecord[] nameServers, DNSResourceRecord[] additionalRecords) {
		this.id = id;
		this.flags = flags;
		this.questions = questions;
		this.answers = answers;
		this.nameServers = nameServers;
		this.additionalRecords = additionalRecords;
	}

	public final short getId() {
		return id;
	}

	public final short getFlags() {
		return flags;
	}

	public final boolean isResponse() {
		return (flags & RESPONSE_MASK) != 0;
	}

	public final int getOpcode() {
		return (flags & OPCODE_MASK) >> OPCODE_SHIFT;
	}

	public final boolean isAuthoritativeAnswer() {
		return (flags & AA_MASK) != 0;
	}

	public final boolean isTruncated() {
		return (flags & TC_MASK) != 0;
	}

	public final boolean isRecursionDesired() {
		return (flags & RD_MASK) != 0;
	}

	public final boolean isRecursionAvailable() {
		return (flags & RA_MASK) != 0;
	}

	public final int getRcode() {
		return (flags & RCODE_MASK);
	}

	public final DNSQuestion[] getQuestions() {
		return questions;
	}

	public final DNSResourceRecord[] getAnswers() {
		return answers;
	}

	public final DNSResourceRecord[] getNameServers() {
		return nameServers;
	}

	public final DNSResourceRecord[] getAdditionalRecords() {
		return additionalRecords;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Message { id: ");
		builder.append(id);
		builder.append(", flags: ");
		builder.append(flags);
		if (questions.length > 0) {
			builder.append(", questions: { ");
			for (int i = 0; i < questions.length; ++i) {
				if (i != 0) {
					builder.append(", ");
				}
				if (questions[i] != null)
					builder.append(questions[i].toString());
				else
					builder.append("null");
			}
			builder.append(" }");
		}
		if (answers.length > 0) {
			builder.append("}, answers: { ");
			for (int i = 0; i < answers.length; ++i) {
				if (i != 0) {
					builder.append(", ");
				}
				if (answers[i] != null)
					builder.append(answers[i].toString());
				else
					builder.append("null");
			}
			builder.append(" }");
		}
		builder.append(" }");
		return builder.toString();
	}
}
