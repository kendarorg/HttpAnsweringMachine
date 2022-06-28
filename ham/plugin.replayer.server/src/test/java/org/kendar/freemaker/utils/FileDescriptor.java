package org.kendar.freemaker.utils;

import org.kendar.replayer.apis.models.SingleScriptLine;
import org.kendar.replayer.storage.ReplayerRow;

import java.util.List;

public class FileDescriptor {
    public byte[] content;
    public List<String> path;
    public String name;
    public String ext;
    public List<SingleScriptLine> lines;
}
