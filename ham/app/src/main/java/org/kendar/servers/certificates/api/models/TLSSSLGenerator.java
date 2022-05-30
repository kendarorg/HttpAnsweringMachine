package org.kendar.servers.certificates.api.models;

import java.util.ArrayList;
import java.util.List;

public class TLSSSLGenerator {
    private List<String> extraDomains = new ArrayList<>();
    private String cn;

    public List<String> getExtraDomains() {
        return extraDomains;
    }

    public void setExtraDomains(List<String> extraDomains) {
        this.extraDomains = extraDomains;
    }


    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }
}
