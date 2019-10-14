package online.xloypaypa.dns.relay.network.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class ChinaIPChecker {

    private static final Logger logger = Logger.getLogger(ChinaIPChecker.class.getName());

    private static ChinaIPChecker chinaIPChecker = new ChinaIPChecker();

    public static ChinaIPChecker getChinaIPChecker() {
        return chinaIPChecker;
    }

    private final ReadWriteLock lock;
    private Map<String, Boolean> activeCache, inactiveCache;

    private ChinaIPChecker() {
        this.lock = new ReentrantReadWriteLock();
        this.activeCache = new ConcurrentHashMap<>();
        this.inactiveCache = new ConcurrentHashMap<>();

        long oneHour = 1000 * 60 * 60;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Lock lock = ChinaIPChecker.this.lock.writeLock();
                try {
                    lock.lock();
                    inactiveCache = activeCache;
                    activeCache = new ConcurrentHashMap<>();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }, oneHour, oneHour);
    }

    public boolean isChinaIp(String ip) throws IOException, InterruptedException {
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            Boolean result = this.activeCache.get(ip);
            if (result == null) {
                result = this.inactiveCache.get(ip);
            }
            if (result == null) {
                result = queryIPCountry(ip);
            }
            this.activeCache.put(ip, result);
            return result;
        } finally {
            lock.unlock();
        }
    }

    private boolean queryIPCountry(String ip) throws IOException, InterruptedException {
        logger.info("sending ip country request for " + ip);
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            return false;
        }
        try {
            JsonObject body = new Gson().fromJson(response.body(), JsonObject.class);
            if (body.get("code").getAsInt() != 0) {
                throw new RuntimeException("taobao ip service error");
            }
            return "CN".equals(body.get("data").getAsJsonObject().get("country_id").getAsString());
        } catch (JsonSyntaxException e) {
            logger.warning("can't parse respond json for " + ip + " from: " + response.body());
            return false;
        }
    }

}
