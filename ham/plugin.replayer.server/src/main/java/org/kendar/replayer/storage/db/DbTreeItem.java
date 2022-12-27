package org.kendar.replayer.storage.db;

import java.util.ArrayList;
import java.util.List;

public class DbTreeItem {

    private String initiator;



    public String getInitiator() {
        return initiator;
    }

    private DbTreeItem parent;

    public DbTreeItem(){
    }

    public void addTarget(DbRow target){
        var httpRequest= target.getRow().getRequest();
        this.initiator = httpRequest.getPathParameter("targetType");
        targets.add(target);

    }

    private List<DbRow> targets = new ArrayList<>();

    public List<DbTreeItem> getChildren() {
        return children;
    }

    private List<DbTreeItem> children = new ArrayList<>();

    public void addChild(DbTreeItem item) {
        item.setParent(this);
        children.add(item);
    }

    public void setParent(DbTreeItem parent) {
        this.parent = parent;
    }

    public DbTreeItem getParent() {
        return parent;
    }

    public DbRow getTarget() {
        return target;
    }
}
