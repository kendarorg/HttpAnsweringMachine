package org.kendar.ham;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

class DnsBuilderImpl implements DnsBuilder {

    private final HamBuilder hamBuilder;
    private ArrayList<ResolvedNames> toAddDnsAndOrTls;
    private boolean generateDns;
    private boolean generateTls;

    DnsBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public String resolve(String requestedDomain) throws HamException {
        var request = hamBuilder.newRequest()
                .withMethod("GET")
                .withPath("/api/dns/lookup/" + requestedDomain);
        var response = hamBuilder.call(request.build());
        return response.getResponseText();
    }

    @Override
    public String addDnsName(String ip, String name) throws HamException {
        var nn = retrieveDnsNames();
        var alreadyExisting = nn
                .stream().filter(d -> d.getDns().equalsIgnoreCase(name)).findFirst();
        var dnsName = new DnsName();
        dnsName.setDns(name);
        dnsName.setIp(ip);
        dnsName.setId(alreadyExisting.isPresent() ? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/mappings",
                        alreadyExisting,
                        () -> alreadyExisting.get().getId()))
                .withJsonBody(dnsName);
        hamBuilder.call(request.build());
        var inserted = retrieveDnsNames()
                .stream().filter(d -> d.getDns().equalsIgnoreCase(name)).findFirst();
        if (inserted.isPresent()) {
            return inserted.get().getId();
        }
        throw new HamException("Missing id");
    }

    @Override
    public void addLocalDnsNames(String... names) throws HamException {
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/dns/mappings")
                .withJsonBody(names);
        hamBuilder.call(request.build());
    }

    @Override
    public void removeDnsName(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/dns/mappings/" + id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<DnsName> retrieveDnsNames() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/mappings");
        return hamBuilder.callJsonList(request.build(), DnsName.class).stream().collect(Collectors.toList());
    }

    @Override
    public String addDnsServer(String address, boolean enabled) throws HamException {
        var alreadyExisting = retrieveDnsServers()
                .stream().filter(d -> d.getAddress().equalsIgnoreCase(address) || d.getResolved().equalsIgnoreCase(address)).findAny();
        var dnsServer = new DnsServer();
        dnsServer.setAddress(address);
        dnsServer.setEnabled(enabled);
        dnsServer.setId(alreadyExisting.isPresent() ? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/dns/servers",
                        alreadyExisting,
                        () -> alreadyExisting.get().getId()))
                .withJsonBody(dnsServer);
        hamBuilder.call(request.build());
        var inserted = retrieveDnsServers()
                .stream().filter(d -> d.getAddress().equalsIgnoreCase(address) || d.getResolved().equalsIgnoreCase(address)).findAny();
        if (inserted.isPresent()) {
            return inserted.get().getId();
        }
        throw new HamException("Missing id");
    }

    @Override
    public void removeDnsServer(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/dns/servers/" + id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<DnsServer> retrieveDnsServers() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/servers");
        return hamBuilder.callJsonList(request.build(), DnsServer.class).stream().collect(Collectors.toList());
    }

    @Override
    public List<ResolvedNames> retrieveResolvedNames() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/dns/list");
        return hamBuilder.callJsonList(request.build(), ResolvedNames.class).stream().collect(Collectors.toList());
    }

    @Override
    public DnsCertsAndNamesBuilder withResolvedNames(Function<ResolvedNames, Boolean> filter) throws HamException {
        toAddDnsAndOrTls = new ArrayList<>();
        toAddDnsAndOrTls.addAll(retrieveResolvedNames().stream()
                .filter(filter::apply).collect(Collectors.toList()));
        return this;
    }

    @Override
    public DnsCertsAndNamesBuilder withResolvedNames(List<ResolvedNames> with) {
        toAddDnsAndOrTls = new ArrayList<>();
        toAddDnsAndOrTls.addAll(with);
        return this;
    }

    @Override
    public DnsCertsAndNamesBuilder addDns() {
        generateDns = true;
        return this;
    }

    @Override
    public DnsCertsAndNamesBuilder addSslTl() {
        generateTls = true;
        return this;
    }

    @Override
    public void createDnsSslTls() throws HamException {
        if (generateDns) {
            new DnsBuilderImpl(hamBuilder)
                    .addLocalDnsNames(toAddDnsAndOrTls.stream().map(ResolvedNames::getName).collect(Collectors.toList())
                            .toArray(new String[]{}));
        }
        if (generateTls) {
            new CertificatesBuilderImpl(hamBuilder)
                    .addAltName(toAddDnsAndOrTls.stream().map(ResolvedNames::getName).collect(Collectors.toList())
                            .toArray(new String[]{}));
        }
    }
}
