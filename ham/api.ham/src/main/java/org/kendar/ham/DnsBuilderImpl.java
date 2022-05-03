package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.stream.Collectors;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

class DnsBuilderImpl implements DnsBuilder {

    private HamBuilder hamBuilder;

    DnsBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public String addDnsName(String ip, String name) throws HamException {
        var alreadyExisting = retrieveDnsNames()
                .stream().filter(d->d.dns.equalsIgnoreCase(name)).findFirst();
        var dnsName = new DnsName();
        dnsName.dns = name;
        dnsName.ip = ip;
        dnsName.id = alreadyExisting.isPresent()?alreadyExisting.get().id:null;
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/mappings",
                        alreadyExisting,
                        ()->alreadyExisting.get().id));
        hamBuilder.call(request.build());
        var inserted = retrieveDnsNames()
                .stream().filter(d->d.dns.equalsIgnoreCase(name)).findFirst();
        if(inserted.isPresent()){
            return inserted.get().id;
        }
        throw new HamException("Missing id");
    }

    @Override
    public void removeDnsName(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/dns/mappings/"+id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<DnsName> retrieveDnsNames() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/mappings");
        return hamBuilder.callJsonList(request.build(), DnsName.class).stream().collect(Collectors.toList());
    }

    @Override
    public String addDnsServer(String address,boolean enabled) throws HamException {
        var alreadyExisting = retrieveDnsServers()
                .stream().filter(d->d.address.equalsIgnoreCase(address)).findAny();
        var dnsServer = new DnsServer();
        dnsServer.address = address;
        dnsServer.enabled = enabled;
        dnsServer.id = alreadyExisting.isPresent()?alreadyExisting.get().id:null;
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/servers",
                        alreadyExisting,
                        ()->alreadyExisting.get().id));
        hamBuilder.call(request.build());
        var inserted = retrieveDnsServers()
                .stream().filter(d->d.address.equalsIgnoreCase(address)).findAny();
        if(inserted.isPresent()){
            return inserted.get().id;
        }
        throw new HamException("Missing id");
    }

    @Override
    public void removeDnsServer(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/dns/servers/"+id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<DnsServer> retrieveDnsServers() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/servers");
        return hamBuilder.callJsonList(request.build(),DnsServer.class).stream().collect(Collectors.toList());
    }

    @Override
    public List<ResolvedNames> retrieveResolvedNames() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/list");
        return hamBuilder.callJsonList(request.build(),ResolvedNames.class).stream().collect(Collectors.toList());
    }
}
