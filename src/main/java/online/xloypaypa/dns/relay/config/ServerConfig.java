package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonObject;

public class ServerConfig {

    private final int port;
    private final SSL ssl;
    private final int numberOfThread;

    ServerConfig(JsonObject serverConfig) {
        this.port = serverConfig.get("port").getAsInt();
        JsonObject sslConfig = serverConfig.has("ssl") ? serverConfig.get("ssl").getAsJsonObject() : null;
        if (sslConfig != null && sslConfig.get("enable").getAsBoolean()) {
            this.ssl = new SSL(true, sslConfig.get("cert").getAsString(), sslConfig.get("privateKey").getAsString());
        } else {
            this.ssl = new SSL(false, null, null);
        }
        int numberOfThread = 4;
        try {
            numberOfThread = serverConfig.get("threads").getAsInt();
        } catch (Exception ignore) {
        }finally {
            this.numberOfThread = numberOfThread;
        }
    }

    public int getNumberOfThread() {
        return numberOfThread;
    }

    public int getPort() {
        return port;
    }

    public SSL getSsl() {
        return ssl;
    }

    public static class SSL {
        private final boolean enable;
        private final String cert, privateKey;

        private SSL(boolean enable, String cert, String privateKey) {
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
