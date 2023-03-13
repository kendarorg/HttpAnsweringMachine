package org.kendar.servers.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JsonFileData {
    private String name;
    private String data;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private byte[] byteData;
    private String stringData;

    public byte[] readAsByte() {
        if (byteData == null) {
            byteData = Base64.getDecoder().decode(this.data);
        }
        return byteData;
    }

    public String readAsString() {
        if (stringData == null) {
            var bytes = readAsByte();
            stringData = new String(bytes, StandardCharsets.UTF_8);
        }
        return stringData;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean matchContentType(String possible) {
        return possible.equalsIgnoreCase(this.type);
    }
}
