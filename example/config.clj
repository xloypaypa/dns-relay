(ns config
  (:import (online.xloypaypa.dns.relay.config Config ServerConfig UpstreamConfig ClientConfig MergerConfig ClientConfig$SSL ServerConfig$SSL)
           (online.xloypaypa.dns.relay.network.client MultiDnsClient)
           (java.util.concurrent Executors)
           (online.xloypaypa.dns.relay.network.merger ChinaDnsMerger)))

(letfn [(buildClientConfig [host port ^ClientConfig$SSL ssl]
          (reify ClientConfig
            (getHost [_] host)
            (getPort [_] port)
            (getSsl [_] ssl)))]
  (let [serverConfig (reify ServerConfig
                       (getNumberOfThread [_] 5)
                       (getPort [_] 6565)
                       (getSsl [_] (new ServerConfig$SSL false nil nil)))

        clientConfigs [(buildClientConfig "127.0.0.1" 1443 (new ClientConfig$SSL false nil))
                       (buildClientConfig "127.0.0.1" 1444
                                          (new ClientConfig$SSL true "upstream.domain.com"))]

        upStreamConfig (reify UpstreamConfig
                         (getMultiDnsClient [_] (new MultiDnsClient clientConfigs (Executors/newFixedThreadPool 5))))

        mergerConfig (reify MergerConfig
                       (isChinaOnly [_ index] (= index 0))
                       (getMerger [_] (new ChinaDnsMerger)))]
    (new Config serverConfig upStreamConfig mergerConfig))
  )
