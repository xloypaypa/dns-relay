package online.xloypaypa.dns.relay.config;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Config config = null;

    public static Config getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    new clojure.lang.RT();
                    try {
                        String configCode = Files.readString(Path.of("./config.clj"));
                        config = (Config) clojure.lang.Compiler.load(new StringReader(configCode));
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        return config;
    }

    private final ServerConfig serverConfig;
    private final UpstreamConfig upstreamConfig;
    private final MergerConfig mergerConfig;

    public Config(ServerConfig serverConfig, UpstreamConfig upstreamConfig, MergerConfig mergerConfig) {
        this.serverConfig = serverConfig;
        this.upstreamConfig = upstreamConfig;
        this.mergerConfig = mergerConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public UpstreamConfig getUpstreamConfig() {
        return upstreamConfig;
    }

    public MergerConfig getMergerConfig() {
        return mergerConfig;
    }
}
