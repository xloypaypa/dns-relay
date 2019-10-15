package online.xloypaypa.dns.relay.network.server.util;

import com.google.protobuf.ByteString;
import coredns.dns.Dns;
import coredns.dns.DnsServiceGrpc;
import io.grpc.stub.StreamObserver;
import online.xloypaypa.dns.relay.config.Config;
import online.xloypaypa.dns.relay.dns.DNSMessage;
import online.xloypaypa.dns.relay.dns.util.DnsMessageParser;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

import java.util.List;
import java.util.stream.Collectors;

public class DnsServiceImpl extends DnsServiceGrpc.DnsServiceImplBase {
    private final MultiDnsClient multiDnsClient;

    public DnsServiceImpl(MultiDnsClient multiDnsClient) {
        this.multiDnsClient = multiDnsClient;
    }

    @Override
    public void query(Dns.DnsPacket request, StreamObserver<Dns.DnsPacket> responseObserver) {
        try {
            List<Dns.DnsPacket> allResponds = this.multiDnsClient.query(request);
            DNSMessage responds = Config.getConfig().getMergerConfig().getMerger()
                    .mergeResponds(DnsMessageParser.parse(request.getMsg().toByteArray()),
                            allResponds.stream().map(now -> {
                                try {
                                    return DnsMessageParser.parse(now.getMsg().toByteArray());
                                } catch (Exception e) {
                                    return null;
                                }
                            }).collect(Collectors.toList()));
            responseObserver.onNext(Dns.DnsPacket.newBuilder().setMsg(ByteString.copyFrom(DnsMessageParser.toBytes(responds))).build());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }
}
