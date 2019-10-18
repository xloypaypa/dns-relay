package online.xloypaypa.dns.relay.network.merger.checker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheAbleChecker implements IPChecker {
    private final ReadWriteLock lock;
    private final IPChecker ipChecker;
    private Map<DomainAndIP, Boolean> activeCache, inactiveCache;

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
        DomainAndIP domainAndIP = new DomainAndIP(domain, ip);
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            Boolean result = this.activeCache.get(domainAndIP);
            if (result == null) {
                result = this.inactiveCache.get(domainAndIP);
            }
            if (result == null) {
                result = this.ipChecker.isIPValid(clientIndex, domain, ip);
            }
            this.activeCache.put(domainAndIP, result);
            return result;
        } finally {
            lock.unlock();
        }
    }

    private static class DomainAndIP {
        private final String domain, ip;

        private DomainAndIP(String domain, String ip) {
            this.domain = domain;
            this.ip = ip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DomainAndIP that = (DomainAndIP) o;
            return Objects.equals(domain, that.domain) &&
                    Objects.equals(ip, that.ip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(domain, ip);
        }
    }
}
