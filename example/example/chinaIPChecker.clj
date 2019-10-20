(ns example.chinaIPChecker
  (:import (org.eclipse.jgit.api Git)
           (java.io File StringReader BufferedReader)
           (online.xloypaypa.dns.relay.network.merger.checker IPChecker)
           (inet.ipaddr IPAddressString)
           (java.util Timer)
           (online.xloypaypa.dns.relay.util TimerTaskWithConstructor)
           (org.apache.commons.io FileUtils)))

(refer 'clojure.string)

(def ^:dynamic allChinaIPRange [])
(def ^Long one-day (* 24 (* 60 (* 60 1000))))

(letfn [(cloneChinaIPList
          []
          (println "cloning china ip list...")
          (let [file (new File "./chinaIPList")]
            (if (.exists file)
              (FileUtils/deleteDirectory file))
            (.call (.setDirectory (.setURI (Git/cloneRepository) "https://github.com/17mon/china_ip_list.git") file)))
          (println "cloned china ip list"))

        (cloneChinaIPListFirstTime []
          (try
            (println "clone china ip list for first time")
            (cloneChinaIPList)
            (catch Exception e
              (.printStackTrace e)
              (println "retrying")
              (cloneChinaIPListFirstTime)
              )))

        (scheduleUpdateChinaIPList []
          (.schedule (new Timer)
                     (new TimerTaskWithConstructor
                          (reify Runnable
                            (run [_]
                              (try
                                (cloneChinaIPList)
                                (alter-var-root #'allChinaIPRange (constantly (loadChinaIPList)))
                                (catch Exception e
                                  (.printStackTrace e))))))
                     one-day, one-day))

        (init []
          (let [file (new File "./chinaIPList")]
            (if (.exists file)
              (println "china ip list exist. skip first clone")
              (cloneChinaIPListFirstTime)))
          (scheduleUpdateChinaIPList))


        (loadChinaIPList []
          (line-seq (BufferedReader. (StringReader. (slurp "./chinaIPList/china_ip_list.txt")))))

        (match [ip ipRange]
          (let [ipAddressString (new IPAddressString ip)
                ipRangeAddressString (new IPAddressString ipRange)]
            (.contains ipRangeAddressString ipAddressString)))]
  (init)
  (alter-var-root #'allChinaIPRange (constantly (loadChinaIPList)))
  (reify IPChecker
    (isIPValid [_ _ _ ip]
      (> (count (filter #(match ip %) allChinaIPRange)) 0))))