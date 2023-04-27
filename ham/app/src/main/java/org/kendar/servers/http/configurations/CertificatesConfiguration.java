package org.kendar.servers.http.configurations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CertificatesConfiguration {
    public final List<String> extraDomains = new ArrayList<>();
    public String cname;
    public long timestamp = Calendar.getInstance().getTimeInMillis();

    public CertificatesConfiguration copy() {
        var result = new CertificatesConfiguration();
        result.cname = cname;
        result.extraDomains.addAll(extraDomains);
        return result;
    }
}
