package org.kendar.ham;

import java.util.List;

public interface CertificatesBuilder {
    public class SubjectAltName {
        private String id;
        private String address;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    void addAltName(String address) throws HamException;
    void removeAltName(String id) throws HamException;
    List<SubjectAltName> retrieveAltNames() throws HamException;
}
