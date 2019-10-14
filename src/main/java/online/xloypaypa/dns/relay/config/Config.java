package online.xloypaypa.dns.relay.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Config config = null;

    public static Config getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    try {
                        config = new Config("./config.json");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        return config;
    }

    private final ServerConfigImpl serverConfigImpl;
    private final UpStreamConfigImpl upStreamConfigImpl;
    private final MergerConfigImpl mergerConfigImpl;

    private Config(String configPath) throws IOException {
        JsonObject config = new Gson().fromJson(Files.readString(Path.of(configPath)), JsonObject.class);
        this.serverConfigImpl = new ServerConfigImpl(config.get("server").getAsJsonObject());
        this.upStreamConfigImpl = new UpStreamConfigImpl(config.get("upstream").getAsJsonObject());
        this.mergerConfigImpl = new MergerConfigImpl(config.get("merger").getAsJsonObject());
    }

    public ServerConfigImpl getServerConfigImpl() {
        return serverConfigImpl;
    }

    public UpStreamConfigImpl getUpStreamConfigImpl() {
        return upStreamConfigImpl;
    }

    public MergerConfigImpl getMergerConfigImpl() {
        return mergerConfigImpl;
    }
}
