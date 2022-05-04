package org.kendar.ham;

public interface DnsCertsAndNamesBuilder {
    DnsCertsAndNamesBuilder addDns();
    DnsCertsAndNamesBuilder addSslTl();
    void createDnsSslTls() throws HamException;
}
