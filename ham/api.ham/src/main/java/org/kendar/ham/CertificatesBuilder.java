package org.kendar.ham;

import java.util.List;

/**
 * To contact the certificates APIs
 *
 * addAltName and removeAltName does a RESTART OF THE HTTPS SERVER
 */
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

    /**
     * Add an alt name to generate the TLS/SSL certificate
     * <B>THIS RESTART THE HTTPS SERVER</B>
     * @param address DNS name
     * @throws HamException
     */
    void addAltName(String address) throws HamException;

    /**
     * Remove the alt-name via id
     * <B>THIS RESTART THE HTTPS SERVER</B>
     * @param id
     * @throws HamException
     */
    void removeAltName(String id) throws HamException;

    /**
     * Retrieve the list of all the alt-names (aka SSL/TLS enabled websites)
     * @return
     * @throws HamException
     */
    List<SubjectAltName> retrieveAltNames() throws HamException;
}
