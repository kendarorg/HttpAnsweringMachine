package org.kendar.ham;

import org.kendar.utils.Sleeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class CertificatesBuilderImpl implements CertificatesBuilder{
    private HamBuilder hamBuilder;

    public CertificatesBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public List<String> addAltName(String ... addresses) throws HamException {

            var result = new ArrayList<String>();
            var request = hamBuilder.newRequest()
                    .withPost()
                    .withPath("/api/ssl")
                    .withJsonBody(addresses);
            hamBuilder.call(request.build());
            Sleeper.sleep(1000);
            return retrieveAltNames().stream()
                    .filter(inserted-> Arrays.stream(addresses).anyMatch(add->add.equalsIgnoreCase(inserted.getAddress())))
                    .map(add->add.getId())
                    .collect(Collectors.toList());
    }

    @Override
    public void removeAltName(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/ssl/"+id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<SubjectAltName> retrieveAltNames() throws HamException {

        var request = hamBuilder.newRequest()
                .withPath("/api/ssl");
        return hamBuilder.callJsonList(request.build(), SubjectAltName.class);
    }
}
