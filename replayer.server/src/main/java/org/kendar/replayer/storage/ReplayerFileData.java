package org.kendar.replayer.storage;

public class ReplayerFileData {
    private String path;
    private String md5;

    public ReplayerFileData(String path, String md5) {

        this.path = path;
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
