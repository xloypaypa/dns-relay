(ns example.config
  (:import (online.xloypaypa.dns.relay.config ServerConfig ServerConfig$SSL UpstreamConfig$ClientConfig UpstreamConfig$ClientConfig$SSL UpstreamConfig MergerConfig Config)
           (online.xloypaypa.dns.relay.network.client MultiDnsClient)
           (java.util.concurrent Executors)
           (online.xloypaypa.dns.relay.network.merger CheckAbleDnsMerger)
           (online.xloypaypa.dns.relay.network.merger.checker IPChecker ChinaOnlyChecker)))

(def serverConfig (reify ServerConfig
                    (getNumberOfThread [_] 4)
                    (getPort [_] 6565)
                    (getSsl [_] (new ServerConfig$SSL false nil nil))))

(letfn [(buildClientConfig
          [host port ^UpstreamConfig$ClientConfig$SSL ssl]
          (reify UpstreamConfig$ClientConfig
            (getHost [_] host)
            (getPort [_] port)
            (getSsl [_] ssl)))]
  (let [clients [(buildClientConfig "172.19.2.5" 1443 (new UpstreamConfig$ClientConfig$SSL false nil))
                 (buildClientConfig "172.19.2.4" 1443 (new UpstreamConfig$ClientConfig$SSL false nil))]]
    (def upstreamConfig (reify UpstreamConfig
                          (getMultiDnsClient [_] (new MultiDnsClient clients (Executors/newFixedThreadPool 4)))))))

(let [chinaOnlyChecker (new ChinaOnlyChecker)
      ipChecker (reify IPChecker
                  (isIPValid [_ clientIndex ip]
                    (if (= clientIndex 0)
                      (.isIPValid chinaOnlyChecker clientIndex ip)
                      true)))]
  (def mergerConfig (reify MergerConfig
                      (getMerger [_] (new CheckAbleDnsMerger ipChecker)))))

(new Config serverConfig upstreamConfig mergerConfig)