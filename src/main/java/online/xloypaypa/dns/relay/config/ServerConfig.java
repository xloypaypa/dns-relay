package online.xloypaypa.dns.relay.config;

import java.util.concurrent.ExecutorService;

public interface ServerConfig {

    ExecutorService getExecutor();

    int getPort();

    SSL getSsl();

    class SSL {
        private final boolean enable;
        private final String cert, privateKey;

        public SSL(boolean enable, String cert, String privateKey) {
            this.enable = enable;
            this.cert = cert;
            this.privateKey = privateKey;
        }

        public boolean isEnable() {
            return enable;
        }

        public String getCert() {
            return cert;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }
}
