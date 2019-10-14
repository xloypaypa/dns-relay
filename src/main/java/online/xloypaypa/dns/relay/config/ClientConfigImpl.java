package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonObject;

public class ClientConfigImpl {

    private final String host;
    private final int port;
    private final SSL ssl;

    public ClientConfigImpl(JsonObject clientConfig) {
        this.host = clientConfig.get("host").getAsString();
        this.port = clientConfig.get("port").getAsInt();
        JsonObject sslConfig = clientConfig.has("ssl") ? clientConfig.get("ssl").getAsJsonObject() : null;
        if (sslConfig != null && sslConfig.get("enable").getAsBoolean()) {
            this.ssl = new SSL(true, sslConfig.get("serverName").getAsString());
        } else {
            this.ssl = new SSL(false, null);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SSL getSsl() {
        return ssl;
    }

    public static class SSL {
        private final boolean enable;
        private final String serverName;

        private SSL(boolean enable, String serverName) {
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
