package org.kendar.replayer.storage;

import org.kendar.utils.LoggerBuilder;

public class PactDataset implements BaseDataset{
    private String name;
    private String rootPath;

    public PactDataset(String name, String rootPath, LoggerBuilder loggerBuilder){
        this.name = name;
        this.rootPath = rootPath;
    }
    @Override
    public String getName() {
        return name;
    }
}
