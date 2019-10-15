package online.xloypaypa.dns.relay.config;

import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

public interface UpstreamConfig {

    MultiDnsClient getMultiDnsClient();

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
