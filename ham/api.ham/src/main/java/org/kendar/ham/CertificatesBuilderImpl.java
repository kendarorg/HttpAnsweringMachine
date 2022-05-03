package org.kendar.ham;

import java.util.List;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

public class CertificatesBuilderImpl implements CertificatesBuilder{
    private HamBuilder hamBuilder;

    public CertificatesBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public String addAltName(String address) throws HamException {
        var alreadyExisting = retrieveAltNames()
                .stream().filter(d->d.address.equalsIgnoreCase(address)).findAny();
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/ssl",
                        alreadyExisting,
                        ()->alreadyExisting.get().id));
        hamBuilder.call(request.build());
        var inserted = retrieveAltNames()
                .stream().filter(d->d.address.equalsIgnoreCase(address)).findAny();
        if(inserted.isPresent()){
            return inserted.get().id;
        }
        throw new HamException("Missing id");
    }

    @Override
    public void removeAltName(String id) {
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
