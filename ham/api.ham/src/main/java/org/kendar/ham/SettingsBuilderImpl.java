package org.kendar.ham;

import org.kendar.utils.ConstantsMime;

public class SettingsBuilderImpl implements SettingsBuilder {
    private final HamBuilder hamBuilder;

    SettingsBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public void upload(String value) throws HamException {
        var request = hamBuilder.newRequest()
                .withMethod("POST")
                .withPath("/api/utils/settings/")
                .withHamFile("newsettings.json", value, ConstantsMime.JSON);
        hamBuilder.call(request.build());
    }

    @Override
    public String download() throws HamException {
        var request = hamBuilder.newRequest()
                .withMethod("GET")
                .withPath("/api/utils/settings/");
        var response = hamBuilder.call(request.build());
        return response.getResponseText();
    }
}
