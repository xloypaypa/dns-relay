package online.xloypaypa.dns.relay.network.server.util;

import coredns.dns.Dns;
import coredns.dns.DnsServiceGrpc;
import io.grpc.stub.StreamObserver;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

public class DnsServiceImpl extends DnsServiceGrpc.DnsServiceImplBase {
    private final MultiDnsClient multiDnsClient;

    public DnsServiceImpl(MultiDnsClient multiDnsClient) {
        this.multiDnsClient = multiDnsClient;
    }

    @Override
    public void query(Dns.DnsPacket request, StreamObserver<Dns.DnsPacket> responseObserver) {
        try {
            Dns.DnsPacket responds = this.multiDnsClient.query(request);
            responseObserver.onNext(responds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }
}
