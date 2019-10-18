(ns example.certificateChecker
  (:import (javax.net.ssl X509TrustManager SSLContext SSLSocket)
           (java.security.cert X509Certificate)
           (online.xloypaypa.dns.relay.network.merger.checker IPChecker)))

(refer 'clojure.string)

; this checker will get the certification from 443 port of the dns ipv4 result.
; still not sure if it's suitable for daily use.

(def x509TrustManager (reify X509TrustManager
                        (getAcceptedIssuers [_])
                        (checkClientTrusted [_ _ _])
                        (checkServerTrusted [_ _ _])))

(letfn [(getSocket [host port]
          (let [context (SSLContext/getInstance "SSL")]
            (.init context nil (into-array [x509TrustManager]) nil)
            (.createSocket (.getSocketFactory context) ^String host ^Integer port)))
        (gainCertificate [host port]
          (try
            (let [^SSLSocket socket (getSocket host port)]
              (.startHandshake socket)
              (.getPeerCertificates (.getSession socket)))
            (catch Exception _
              [])))
        (getAllDomainInCertificate [^X509Certificate certificate]
          (try
            (let [allNames (.getSubjectAlternativeNames certificate)
                  dnsName (filter #(and (= 2 (nth % 0)) (not (nil? (nth % 1)))) allNames)]
              (map #(nth % 1) dnsName))
            (catch Exception _
              [])))
        (getAllDomainInCertificates [certificates]
          (reduce concat (map #(getAllDomainInCertificate %) certificates) ))

        (isValidDomain [^String domain]
          (and (not (nil? domain))
               (not (blank? domain))
               (not (starts-with? domain "."))
               (not (ends-with? domain ".."))))
        (isValidWildcard [^String domain]
          (and (starts-with? domain "*.")
               (nil? (index-of domain "*" 1))))

        (isDomainMatchWildcard [^String domain ^String pattern]
          (let [suffix (subs pattern 1)
                suffixStartIndexInHostname (- (.length domain) (.length suffix))]
            (and (> suffixStartIndexInHostname 0)
                 (ends-with?  domain suffix)
                 (nil? (last-index-of domain "." (- suffixStartIndexInHostname 1))))))

        (isMatch [^String domain ^String pattern]
          (and (isValidDomain domain) (isValidDomain pattern)
               (if (not (nil? (.indexOf pattern "*")))
                 (and (isValidWildcard pattern) (isDomainMatchWildcard domain pattern))
                 (.equals domain pattern))))
        ]
  (reify IPChecker
    (isIPValid [_ _ domain ip]
      (println "validate" domain "with" ip)
      (let [certificates (gainCertificate ip 443)
            domainInCertificate (getAllDomainInCertificates certificates)
            matchedDomain (filter #(isMatch domain %) domainInCertificate)]
        (> (count matchedDomain) 0)))))