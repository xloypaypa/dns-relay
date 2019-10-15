package online.xloypaypa.dns.relay.network.merger;

import com.google.protobuf.ByteString;
import coredns.dns.Dns;
import online.xloypaypa.dns.relay.dns.DNSMessage;
import online.xloypaypa.dns.relay.dns.DNSParseException;
import online.xloypaypa.dns.relay.dns.DNSQuestion;
import online.xloypaypa.dns.relay.dns.DNSResourceRecord;
import online.xloypaypa.dns.relay.dns.util.DnsMessageParser;
import online.xloypaypa.dns.relay.dns.util.NameParser;
import online.xloypaypa.dns.relay.network.merger.checker.IPCheckException;
import online.xloypaypa.dns.relay.network.merger.checker.IPChecker;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CheckAbleDnsMerger extends DefaultMerger {
    private static final Logger logger = Logger.getLogger(CheckAbleDnsMerger.class.getName());

    private final IPChecker ipChecker;

    public CheckAbleDnsMerger(IPChecker ipChecker) {
        this.ipChecker = ipChecker;
    }

    @Override
    public Dns.DnsPacket mergeResponds(Dns.DnsPacket request, List<Dns.DnsPacket> responds) {
        try {
            List<CheckableDNsMessage> checkableDNSMessages = getCheckableDNSMessages(responds);
            AnswersMapForQuestions answersMapForQuestions = new AnswersMapForQuestions();
            DNSQuestion[] questions = DnsMessageParser.parse(request.getMsg().toByteArray()).getQuestions();
            for (DNSQuestion now : questions) {
                if (now.getQType() == 1) {
                    List<Answer> answerForDomain = getNoBlockedAnswerForDomain(now.getName(), checkableDNSMessages);
                    answersMapForQuestions.update(now.getName(), answerForDomain);
                }
            }
            return buildRespond(responds, checkableDNSMessages, answersMapForQuestions);
        } catch (Exception e) {
            e.printStackTrace();
            return super.mergeResponds(request, responds);
        }
    }

    private Dns.DnsPacket buildRespond(List<Dns.DnsPacket> responds, List<CheckableDNsMessage> checkableDNsMessages, AnswersMapForQuestions answersMapForQuestions) throws DNSParseException {
        for (int i = 0; i < responds.size(); i++) {
            Dns.DnsPacket dnsPacket = responds.get(i);
            CheckableDNsMessage checkableDNsMessage = checkableDNsMessages.get(i);
            if (dnsPacket == null || checkableDNsMessage == null) continue;

            DNSMessage dnsMessage = DnsMessageParser.parse(dnsPacket.getMsg().toByteArray());
            List<DNSResourceRecord> answers = checkableDNsMessage.answers.stream()
                    .filter(now -> !now.checked)
                    .map(now -> now.dnsResourceRecord)
                    .collect(Collectors.toList());

            for (List<Answer> filteredAnswers : answersMapForQuestions.answerMap.values()) {
                answers.addAll(filteredAnswers.stream().map(now -> now.dnsResourceRecord).collect(Collectors.toList()));
            }

            DNSResourceRecord[] finalAnswers = new DNSResourceRecord[answers.size()];
            for (int j = 0; j < finalAnswers.length; j++) {
                finalAnswers[j] = answers.get(j);
            }
            DNSMessage finalDnsMessage = new DNSMessage(dnsMessage.getId(), dnsMessage.getFlags(), dnsMessage.getQuestions(),
                    finalAnswers,
                    dnsMessage.getNameServers(), dnsMessage.getAdditionalRecords());

            return Dns.DnsPacket.newBuilder().setMsg(ByteString.copyFrom(DnsMessageParser.toBytes(finalDnsMessage))).build();
        }
        throw new RuntimeException("all responds is null");
    }

    private List<Answer> getNoBlockedAnswerForDomain(String domain, List<CheckableDNsMessage> checkableDNsMessages) throws DNSParseException, IPCheckException {
        for (int index = 0; index < checkableDNsMessages.size(); index++) {
            CheckableDNsMessage respond = checkableDNsMessages.get(index);
            if (respond == null) {
                logger.warning("can't get respond from #" + index);
                continue;
            }

            List<Answer> answersRelated = getAnswersForQuestion(domain, respond);
            boolean isBlock = checkIfNeedBlockAnswer(index, answersRelated.stream().filter(now -> now.dnsResourceRecord.getRType() == 1).collect(Collectors.toList()));
            for (Answer answer : answersRelated) {
                answer.checked = true;
            }
            if (!isBlock) {
                logger.info("for " + domain + ", accepted #" + respond.index + "'s respond");
                return answersRelated;
            } else {
                logger.info("for " + domain + ", blocked #" + respond.index + "'s respond");
            }
        }
        throw new RuntimeException("all responds blocked");
    }

    private boolean checkIfNeedBlockAnswer(int clientIndex, List<Answer> aTypeAnswer) throws IPCheckException {
        List<String> ips = getRespondIP(aTypeAnswer);
        boolean isValid = false;
        for (String ip : ips) {
            if (this.ipChecker.isIPValid(clientIndex, ip)) {
                isValid = true;
                break;
            }
        }
        return !isValid;
    }

    private List<CheckableDNsMessage> getCheckableDNSMessages(List<Dns.DnsPacket> responds) {
        List<CheckableDNsMessage> result = new ArrayList<>();
        for (int i = 0; i < responds.size(); i++) {
            Dns.DnsPacket now = responds.get(i);
            try {
                DNSResourceRecord[] answers = DnsMessageParser.parse(now.getMsg().toByteArray()).getAnswers();
                result.add(new CheckableDNsMessage(i, answers));
            } catch (Exception e) {
                result.add(null);
            }
        }
        return result;
    }

    private List<String> getRespondIP(List<Answer> answersForQuestion) {
        List<String> result = new ArrayList<>();
        for (Answer answer : answersForQuestion) {
            byte[] ipData = answer.dnsResourceRecord.getRData();
            StringBuilder ip = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                ip.append((int) ipData[i] & 0xff);
                if (i != 3) {
                    ip.append('.');
                }
            }
            result.add(ip.toString());
        }
        return result;
    }

    private List<Answer> getAnswersForQuestion(String domain, CheckableDNsMessage respond) throws DNSParseException {
        List<Answer> result = new ArrayList<>();
        for (Answer now : respond.answers) {
            if (!domain.equals(now.dnsResourceRecord.getName())) continue;

            if (now.dnsResourceRecord.getRType() == 1) {
                result.add(now);
            } else if (now.dnsResourceRecord.getRType() == 5) {
                result.add(now);
                result.addAll(getAnswersForQuestion(NameParser.parseName(ByteBuffer.wrap(now.dnsResourceRecord.getRData())), respond));
            }
        }
        return result;
    }

    private static class AnswersMapForQuestions {
        private Map<String, List<Answer>> answerMap;

        private AnswersMapForQuestions() {
            this.answerMap = new HashMap<>();
        }

        private void update(String domain, List<Answer> answers) {
            this.answerMap.put(domain, answers);
        }
    }

    private static class CheckableDNsMessage {
        private int index;
        private List<Answer> answers;

        private CheckableDNsMessage(int index, DNSResourceRecord[] answers) {
            this.index = index;
            this.answers = new ArrayList<>();
            for (DNSResourceRecord now : answers) {
                this.answers.add(new Answer(now));
            }
        }
    }

    private static class Answer {
        private DNSResourceRecord dnsResourceRecord;
        private boolean checked;

        private Answer(DNSResourceRecord dnsResourceRecord) {
            this.dnsResourceRecord = dnsResourceRecord;
            this.checked = false;
        }
    }
}