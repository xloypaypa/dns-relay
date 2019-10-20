# dns-relay
>一个基于grpc的dns中继。多个上游dns server，并对其结果进行筛选整合。

>a dns relay base on grpc. support merge responds from multiple upstream dns server.

# 快速开始 quick start 
>自己打image：
>* gradle composeUp
>* nslookup -port=1053 www.google.com 127.0.0.1
>
>用我打好的image
>* 写Dockerfile
>   * From xloypaypa/dns-relay
>   * ADD ./your/clojure/config/path/config.clj /
>   * docker build

>build docker images by yourself:
>* gradle composeUp
>* nslookup -port=1053 www.google.com 127.0.0.1
>
>use my docker image
>* write your Dockerfile
>   * From xloypaypa/dns-relay
>   * ADD ./your/clojure/config/path/config.clj /
>   * docker build

# 使用建议 using suggestions
>众所周知，dns是用的udp，并且大多数时候使用53端口。所以建议结合[coredns](https://coredns.io/)，并使用[grpc](https://coredns.io/plugins/grpc/)插件（似乎这个插件在tls会有点小问题）。
>
>由于这个东西的重点是在于对dns结果的过滤，也为了减少工作量。所以除了对ip的验证结果有cache（随手撸的一个，要用的话自求多福）之外，没有任何的缓存。建议使用时和例子里一样，前后都套上coredns，并使用[cache](https://coredns.io/plugins/cache/)插件。
>
>由于配置是clojure写成的。一方面可以只把config.clj当作完整的配置；另一方面config.clj可以只是一个入口，所以可以在里面写读文件、调api拿上游配置之类的骚操作。

>As we know, dns is based on upd, and using port 53 majorly. So I suggest to use it with [coredns](https://coredns.io/), And use [grpc](https://coredns.io/plugins/grpc/) plugin (looks like this plugin have some issue with tls)。
>
>Because of the majority feature of thie project it filter the results of dns request. Also because I'm lazy :p. So there aren't any cache except the cache for result of ip validation (I wrote it, not really stable). So my suggestion is use like example. Set up coredns servers on the front and back for this. And enable [cache](https://coredns.io/plugins/cache/) plugin。 

# 配置 configuration
>[example](https://github.com/xloypaypa/dns-relay/tree/master/example)里的配置已经比较清楚了。
>* 配置只能用clojure写，而且只能是在运行路径下的clojure.clj，并且这段Clojure脚本要返回一个online.xloypaypa.dns.relay.config.Config对象。
>* Config对象只需要new出来就好，需要三个参数：serverConfig，upstreamConfig和mergerConfig。
>* ServerConfig就直接实现ServerConfig。
>   * 端口就老老实实给个端口
>   * getExecutor就是你自己new一个也好，怎么样也好给出一个executorService就成。
>   * ssl的话，如果不开就是false然后后面两个穿空。如果要开就是true publicKeyPathString privateKeyPathString
>* upstream就两件事情，一个是给好线程池，一个是给出上游dns server
>   * getExecutor和server的功能一样，可以考虑和server共享线程池。
>   * clients就是一个List<ClientConfig>
>       * getHost, getPort就是这个上游服务器的地址和端口
>       * getSsl和服务器稍微有点不一样（毕竟没有privateKey）。不用ssl的话就false，nil；要ssl的话就true，证书对应的域名。
>* merger这个就比较厉害了。目前有两个支持的merger，一个是DefaultMerger，一个是CheckAbleDnsMerger。
>   * 总的来说还是实现MergerConfig接口，这个接口就只用实现一个方法getMerger，返回值是一个MultiRespondsMerger接口，DefaultMerger和CheckAbleDnsMerger都是MultiRespondsMerger的实现。如果有特殊需求，配置里直接用clojure写也好，java里加类也行。
>   * default就是根据clients配置的顺序来的。以第一个有效的返回为返回。也就是说，如果第一个client返回的dns responds里说查询失败了，而第二个client返回了ip等结果。那么依然会以第一client那个失败的结果返回。当且仅当第一个client由于连不上、超时等情况导致没有返回时才会使用后续的dns。也就是说，默认的merger，认为排在前面的dns server更加可靠。
>   * CheckAbleDnsMerger这个就更厉害啦。要求传入一个IPChecker。
>       * IPChecker就只做isIPValid一件事。参数是clientIndex和IP地址。clientIndex就是前面upstream的clients的下标（毕竟是配置文件，令人难受就难受吧）。
>       * ~~ChinaOnlyChecker**是调淘宝的ip库**来判断ip是否是国内ip，如果404则认为是国外ip。example里就用的这个，并套了一个cache。~~
>       * CacheAbleChecker要求传入一个做事的IPChecker和一个缓存清理时间。**手写的cache，就自求多福吧。**
>       * example就是一个典型的只采纳114dns的国内ip，国外ip全部视为invalid的例子。因为只有在client下标为0的时候才去调ChinaOnlyChecker。

> the [example](https://github.com/xloypaypa/dns-relay/tree/master/example) is very clear. Things could be easier if you read the code directly.

# TODO
>* ~~重构代码，拆分把merger独立出来，毕竟merge其实和client去拿到多个dns server的结果这件事情其实没什么关系。~~
>* 重构dns请求解析代码……简直就是一坨屎……
>* ~~引入clojure进行配置。**主要是因为目前判断ip的国家是直接用的淘宝的ip库**。显然，用json配置这个是不可能的，但又不想写死，所以妥妥的clojure。~~
>* 引入ip校验。在merger里加一个配置叫做"verify":true，可结合chinaOnly使用。两个做的事情其实类似，只是一个是验证ip的归属地，一个是验证ip的证书是不是和查询的domain一致（这里默认全世界所有网站都是https的，如果连https都没有你还指望他能又cdn？能指望他对不同地区又多个ip？）。
>* ~~merger传参优化。直接给已经转成对象的dns message更好些。~~
>* 引入靠谱的缓存框架。
