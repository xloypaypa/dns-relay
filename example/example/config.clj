(ns example.config
  (:import (online.xloypaypa.dns.relay.config ServerConfig ServerConfig$SSL UpstreamConfig$ClientConfig UpstreamConfig$ClientConfig$SSL UpstreamConfig MergerConfig Config)
           (java.util.concurrent Executors)
           (online.xloypaypa.dns.relay.network.merger CheckAbleDnsMerger)
           (online.xloypaypa.dns.relay.network.merger.checker IPChecker CacheAbleChecker)))

(def serverConfig (reify ServerConfig
                    (getExecutor [_] (Executors/newFixedThreadPool 4))
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
                          (getClientConfigs [_] clients)
                          (getExecutor [_] (Executors/newFixedThreadPool 4))))))

(let [chinaIPcChecker (load-file "/chinaIPChecker.clj")
      ;certificateChecker (load-file "/certificateChecker.clj")
      ;chinaOnlyAndMustCanGetCertificateChecker (reify IPChecker
      ;                                           (isIPValid [_ clientIndex domain ip]
      ;                                             (and (.isIPValid chinaOnlyChecker clientIndex domain ip)
      ;                                                  (.isIPValid certificateChecker clientIndex domain ip))))
      cacheAbleCheck (new CacheAbleChecker chinaIPcChecker (* 1000 (* 60 (* 60))))
      ipChecker (reify IPChecker
                  (isIPValid [_ clientIndex domain ip]
                    (if (= clientIndex 0)
                      (.isIPValid cacheAbleCheck clientIndex domain ip)
                      true)))]
  (def mergerConfig (reify MergerConfig
                      (getMerger [_] (new CheckAbleDnsMerger ipChecker)))))

(new Config serverConfig upstreamConfig mergerConfig)