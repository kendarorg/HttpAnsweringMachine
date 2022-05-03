package org.kendar.ham;

import java.util.List;

public interface CertificatesBuilder {
    public class SubjectAltName {
        public String id;
        public String address;
    }

    String addAltName(String address) throws HamException;
    void removeAltName(String id);
    List<SubjectAltName> retrieveAltNames() throws HamException;
}
