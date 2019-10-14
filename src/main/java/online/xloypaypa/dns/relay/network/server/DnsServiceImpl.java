package online.xloypaypa.dns.relay.network.server;

import coredns.dns.Dns;
import coredns.dns.DnsServiceGrpc;
import io.grpc.stub.StreamObserver;
import online.xloypaypa.dns.relay.network.client.DnsClientBuilder;

class DnsServiceImpl extends DnsServiceGrpc.DnsServiceImplBase {
    private final DnsClientBuilder dnsClientBuilder;

    DnsServiceImpl(DnsClientBuilder dnsClientBuilder) {
        this.dnsClientBuilder = dnsClientBuilder;
    }

    @Override
    public void query(Dns.DnsPacket request, StreamObserver<Dns.DnsPacket> responseObserver) {
        try {
            Dns.DnsPacket responds = this.dnsClientBuilder.buildDnsClient().query(request);
            responseObserver.onNext(responds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }
}
