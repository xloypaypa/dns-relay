package online.xloypaypa.dns.relay.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Config config = null;

    public static Config loadConfig(String configPath) throws IOException {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config(configPath);
                }
            }
        }
        return config;
    }

    private final ServerConfig serverConfig;
    private final UpStreamConfig upStreamConfig;

    private Config(String configPath) throws IOException {
        JsonObject config = new Gson().fromJson(Files.readString(Path.of(configPath)), JsonObject.class);
        this.serverConfig = new ServerConfig(config.get("server").getAsJsonObject());
        this.upStreamConfig = new UpStreamConfig(config.get("upstream").getAsJsonObject());
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public UpStreamConfig getUpStreamConfig() {
        return upStreamConfig;
    }
}
