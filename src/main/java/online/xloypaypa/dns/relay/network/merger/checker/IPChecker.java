package online.xloypaypa.dns.relay.network.merger.checker;

public interface IPChecker {
    boolean isIPValid(int clientIndex, String ip) throws IPCheckException;
}
