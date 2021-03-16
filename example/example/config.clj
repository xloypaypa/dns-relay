;个人建议，从下往上看这个文件。
;因为这个文件最终是new了一个Config的对象，并且把这个对象作为这个脚本的返回值，返回给java代码。

(ns example.config
  (:import (online.xloypaypa.dns.relay.config ServerConfig ServerConfig$SSL UpstreamConfig$ClientConfig UpstreamConfig$ClientConfig$SSL UpstreamConfig MergerConfig Config)
           (java.util.concurrent Executors)
           (online.xloypaypa.dns.relay.network.merger CheckAbleDnsMerger)
           (online.xloypaypa.dns.relay.network.merger.checker IPChecker CacheAbleChecker)))

;这里主要是配置dns-relay自身作为一个服务的情况。这里定义的是4个线程，监听6565端口，并且disable了ssl。
(def serverConfig (reify ServerConfig
                    (getExecutor [_] (Executors/newFixedThreadPool 4))
                    (getPort [_] 6565)
                    (getSsl [_] (new ServerConfig$SSL false nil nil))))

;这一块最终都是为了配置upstreamConfig。
;这里首先定义了buildClientConfig函数。其实就是包一层，避免直接操作reify来实现java借口。
(letfn [(buildClientConfig
          [host port ^UpstreamConfig$ClientConfig$SSL ssl]
          (reify UpstreamConfig$ClientConfig
            (getHost [_] host)
            (getPort [_] port)
            (getSsl [_] ssl)))]
  ;这里就是调用buildClientConfig来生成上游dns的配置。这里disable了ssl。
  (let [clients [(buildClientConfig "172.19.2.5" 1443 (new UpstreamConfig$ClientConfig$SSL false nil))
                 (buildClientConfig "172.19.2.4" 1443 (new UpstreamConfig$ClientConfig$SSL false nil))]]
    ;这里实现upstreamConfig。就只是把上游dns的配置穿进去。然后定义了访问上游dns的线程数为4个。
    (def upstreamConfig (reify UpstreamConfig
                          (getClientConfigs [_] clients)
                          (getExecutor [_] (Executors/newFixedThreadPool 4))))))

;这一块定义mergerConfig。这里就是真正处理合并逻辑的地方。
;这里加载了一个chinaIPChecker。
(let [chinaIPcChecker (load-file "/chinaIPChecker.clj")
      ;这里给了一个进一步验证dns解析结果的代码。大致来说是，拿结果去尝试访问对应ip，看证书是否符合域名。我自己没有这么干，所以注释掉。
      ;certificateChecker (load-file "/certificateChecker.clj")
      ;chinaOnlyAndMustCanGetCertificateChecker (reify IPChecker
      ;                                           (isIPValid [_ clientIndex domain ip]
      ;                                             (and (.isIPValid chinaOnlyChecker clientIndex domain ip)
      ;                                                  (.isIPValid certificateChecker clientIndex domain ip))))

      ;这里其实定的是一个checker。这个CacheAbleChecker只是对一般的checker做了一层包装，以及添加了cache。
      ;这个checker的职责就只是说看这个domian解析出来的ip是不是符合某个规则而已。
      cacheAbleCheck (new CacheAbleChecker chinaIPcChecker (* 1000 (* 60 (* 60))))

      ;这里又实现了一个新的checker。本质是对cacheAbleCheck的再包装。这里添加的逻辑是，只对第一个client index做check，其他不管。
      ;client index和上面dns上游配置的下标是对应的。
      ipChecker (reify IPChecker
                  (isIPValid [_ clientIndex domain ip]
                    (if (= clientIndex 0)
                      (.isIPValid cacheAbleCheck clientIndex domain ip)
                      true)))]

  ;最后，生成了一个mergerConfig。这个merger用的CheckAbleDnsMerger。就是按照下标顺序对每个上游的dns做check。返回第一个check通过的结果。
  (def mergerConfig (reify MergerConfig
                      (getMerger [_] (new CheckAbleDnsMerger ipChecker)))))

;对于Config主要分三块，serverConfig upstreamConfig mergerConfig。分别定义在上面的代码。
(new Config serverConfig upstreamConfig mergerConfig)