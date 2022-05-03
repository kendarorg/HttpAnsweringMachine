package org.kendar.ham;

public interface HamBasicBuilder  {
    HamBasicBuilder withPort(int port);
    HamBasicBuilder withDns(String ip,int port);
    HamBasicBuilder withSocksProxy(String ip,int port);
    HamBasicBuilder withDns(String ip);
    HamBasicBuilder withHttps();
    DnsBuilder dns();
    CertificatesBuilder certificates();
    ProxyBuilder proxyes();

}
