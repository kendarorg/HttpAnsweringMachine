package org.kendar.ham;

public interface HamBasicBuilder  {
    HamBasicBuilder withPort(int port);
    HamBasicBuilder withHttps();
    DnsBuilder dns();
    CertificatesBuilder certificates();
    ProxyBuilder proxyes();
}
