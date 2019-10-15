package online.xloypaypa.dns.relay.network.merger.checker;

public class IPCheckException extends Exception {
    public IPCheckException() {
    }

    public IPCheckException(String message) {
        super(message);
    }

    public IPCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public IPCheckException(Throwable cause) {
        super(cause);
    }

    public IPCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
