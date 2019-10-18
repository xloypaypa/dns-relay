package online.xloypaypa.dns.relay.network.merger.checker;

public interface IPChecker {
    boolean isIPValid(int clientIndex, String domain, String ip) throws IPCheckException;
}
