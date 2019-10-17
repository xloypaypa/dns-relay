package online.xloypaypa.dns.relay.network.merger.checker;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheAbleChecker implements IPChecker {
    private final ReadWriteLock lock;
    private final IPChecker ipChecker;
    private Map<String, Boolean> activeCache, inactiveCache;

    public CacheAbleChecker(IPChecker ipChecker, long period) {
        this.lock = new ReentrantReadWriteLock();
        this.ipChecker = ipChecker;
        this.activeCache = new ConcurrentHashMap<>();
        this.inactiveCache = new ConcurrentHashMap<>();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Lock lock = CacheAbleChecker.this.lock.writeLock();
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
        }, period, period);

    }

    @Override
    public boolean isIPValid(int clientIndex, String domain, String ip) throws IPCheckException {
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            Boolean result = this.activeCache.get(ip);
            if (result == null) {
                result = this.inactiveCache.get(ip);
            }
            if (result == null) {
                result = this.ipChecker.isIPValid(clientIndex, domain, ip);
            }
            this.activeCache.put(ip, result);
            return result;
        } finally {
            lock.unlock();
        }
    }
}
