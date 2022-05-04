package org.kendar.ham;

import java.util.List;
import java.util.UUID;
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
        var nn = retrieveDnsNames();
        var alreadyExisting = nn
                .stream().filter(d-> d.getDns().equalsIgnoreCase(name)).findFirst();
        var dnsName = new DnsName();
        dnsName.setDns(name);
        dnsName.setIp(ip);
        dnsName.setId(alreadyExisting.isPresent()? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/mappings",
                        alreadyExisting,
                        ()-> alreadyExisting.get().getId()))
                .withJsonBody(dnsName);
        hamBuilder.call(request.build());
        var inserted = retrieveDnsNames()
                .stream().filter(d-> d.getDns().equalsIgnoreCase(name)).findFirst();
        if(inserted.isPresent()){
            return inserted.get().getId();
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
                .stream().filter(d-> d.getAddress().equalsIgnoreCase(address)).findAny();
        var dnsServer = new DnsServer();
        dnsServer.setAddress(address);
        dnsServer.setEnabled(enabled);
        dnsServer.setId(alreadyExisting.isPresent()? alreadyExisting.get().getId() :null);
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/servers",
                        alreadyExisting,
                        ()-> alreadyExisting.get().getId()));
        hamBuilder.call(request.build());
        var inserted = retrieveDnsServers()
                .stream().filter(d-> d.getAddress().equalsIgnoreCase(address)).findAny();
        if(inserted.isPresent()){
            return inserted.get().getId();
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
