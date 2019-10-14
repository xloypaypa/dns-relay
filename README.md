# dns-relay
>一个基于grpc的dns中继。多个上游dns server，并对其结果进行筛选整合。

>a dns relay base on grpc. support merge responds from multiple upstream dns server.

# 使用 how to use
>众所周知，dns是用的udp，并且大多数时候使用53端口。所以建议结合[coredns](https://coredns.io/)，并使用[grpc](https://coredns.io/plugins/grpc/)插件（似乎这个插件在tls会有点小问题）。
>
>简单版(用我打好的image)：
>参考[example](https://github.com/xloypaypa/dns-relay/tree/master/example)里的dockerfile。改改config.json,如果有的话吧key加上。然后就完事了。
>
>复杂版(自己打image)：
>就gradle clean build一下拿到一个jar包。然后把jar包扔进container里就成。如你所见，这是个java项目，而且配置文件路径写死了./config.json……所以就自求多幅吧。

# 配置 config
>[example](https://github.com/xloypaypa/dns-relay/tree/master/example)里的配置已经比较清楚了。只有以下几点需要说一下。
>* server里的thread是配置grpc netty的executor的。用的是fixedThreadPool。默认4个。
>* upstream里type最好就只multi。因为作者比较菜，不能一步到位，所以早先实现了一个简单代理。之后会只支持multi，并且删除这个配置。
>* clients里就是你的多个上游的配置，当然是允许多个的。无非是host，port，ssl之类的，然后如果ssl开了的话，记得要配serverName。（clients这个名字不是特别合适，回头继续修改）
>* merger这个就比较厉害了。目前有两个支持的merger，一个是default，一个是chinaIP。
>   * default就是根据clients配置的顺序来的。以第一个有效的返回为返回。也就是说，如果第一个client返回的dns responds里说查询失败了，而第二个client返回了ip等结果。那么依然会以第一client那个失败的结果返回。当且仅当第一个client由于连不上、超时等情况导致没有返回时才会使用后续的dns。也就是说，默认的merger，认为排在前面的dns server更加可靠。
>   * chinaIP这个就更厉害啦。他会去读client的具体配置，看他是不是“chinaOnly”的。如果是，那么对于这一个client的结果进行检查，如果他返回了一个非CN的ip，那么就丢弃尝试丢弃相关的查询结果，并看看后面的client有没有是CN ip或者不要求chinaOnly的。如果有，那就移花接木。如果全部都丢弃了，那么就直接按default的规则来。（这里有个feature，如果你一次查询多个domain，然后其中一个domain全部丢弃了，另一个其实还能merge。但是由于实现那里是直接抛异常，是因为异常才走default的，所以这种情况直接走default，不会merge第二个请求的answer。）

# TODO
>* 重构代码，拆分把merger独立出来，毕竟merge其实和client去拿到多个dns server的结果这件事情其实没什么关系。
>* 重构dns请求解析代码……简直就是一坨屎……
>* 引入clojure进行配置。主要是因为目前判断ip的国家是直接用的淘宝的ip库。显然，用json配置这个是不可能的，但又不想写死，所以妥妥的clojure。
>* 引入ip校验。在merger里加一个配置叫做"verify":true，可结合chinaOnly使用。两个做的事情其实类似，只是一个是验证ip的归属地，一个是验证ip的证书是不是和查询的domain一致（这里默认全世界所有网站都是https的，如果连https都没有你还指望他能又cdn？能指望他对不同地区又多个ip？）。