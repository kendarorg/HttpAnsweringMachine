package org.kendar.ham;

public class SettingsBuilderImpl implements SettingsBuilder{
    private final HamBuilder hamBuilder;

    SettingsBuilderImpl(HamBuilder hamBuilder) {
        this.hamBuilder = hamBuilder;
    }

    @Override
    public void upload(String value) throws HamException {
        var request = hamBuilder.newRequest()
                .withMethod("POST")
                .withPath("/api/utils/settings/")
                .withText(value);
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
