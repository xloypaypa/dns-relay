package online.xloypaypa.dns.relay.network.merger.checker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class ChinaOnlyChecker implements IPChecker {

    private static final Logger logger = Logger.getLogger(ChinaOnlyChecker.class.getName());

    @Override
    public boolean isIPValid(int clientIndex, String domain, String ip) throws IPCheckException {
        logger.info("sending ip country request for " + ip);
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new IPCheckException(e);
        }
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
