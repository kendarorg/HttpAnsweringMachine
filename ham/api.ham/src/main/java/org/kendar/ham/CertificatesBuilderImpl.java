package org.kendar.ham;

import java.util.List;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

class CertificatesBuilderImpl implements CertificatesBuilder{
    private HamBuilder hamBuilder;

    public CertificatesBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public void addAltName(String address) throws HamException {
        var alreadyExisting = retrieveAltNames()
                .stream().filter(d-> d.getAddress().equalsIgnoreCase(address)).findAny();
        var altName = new SubjectAltName();
        altName.setAddress(address);
        altName.setId(alreadyExisting.isPresent()? alreadyExisting.get().getId() :null);
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/ssl",
                        alreadyExisting,
                        ()-> alreadyExisting.get().getId()))
                .withJsonBody(altName);
        hamBuilder.call(request.build());
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
