package online.xloypaypa.dns.relay.config;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface UpstreamConfig {

    List<ClientConfig> getClientConfigs();

    ExecutorService getExecutor();

    interface ClientConfig {

        String getHost();

        int getPort();

        SSL getSsl();

        class SSL {
            private final boolean enable;
            private final String serverName;

            public SSL(boolean enable, String serverName) {
                this.enable = enable;
                this.serverName = serverName;
            }

            public boolean isEnable() {
                return enable;
            }

            public String getServerName() {
                return serverName;
            }
        }
    }
}
