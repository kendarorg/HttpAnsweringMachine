package org.kendar.replayer.engine.db;


import org.kendar.janus.cmd.interfaces.JdbcCommand;

import java.util.ArrayList;
import java.util.List;

public class DbTreeItem {

    private String initiator;


    public String getInitiator() {
        return initiator;
    }

    private DbTreeItem parent;

    public DbTreeItem() {
    }

    public void addTarget(DbRow target) {
        var httpRequest = target.getRow().getRequest();
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

    public List<DbRow> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        return toString(0);

    }

    private String toString(int level) {
        var pad = "";
        for (var i = 0; i < level; i++) {
            pad += "  ";
        }

        var result = "";
        result += pad + "{\n";
        result += pad + " " + initiator + "\n";

        if (targets.size() > 0) {
            result += pad + " targets:[\n";
            for (int i = 0; i < targets.size(); i++) {
                DbRow target = targets.get(i);
                result += addPad(target.getRequest(), pad + "  ");
                if (i < (targets.size() - 1)) {
                    result += "\n";
                }
            }
            result += pad + " ],\n";
        }
        if (children.size() > 0) {
            result += pad + " children:[\n";
            for (int i = 0; i < children.size(); i++) {
                DbTreeItem child = children.get(i);
                result += child.toString(level + 1);
                if (i < (children.size() - 1)) {
                    result += "\n";
                }
            }
            result += pad + " ]\n";
        }
        result += pad + "}\n";

        return result;
    }

    private String addPad(JdbcCommand request, String level) {
        var splitted = request.toString().split("\n");
        var result = "";
        for (int i = 0; i < splitted.length; i++) {
            String sp = splitted[i];
            while (sp.startsWith("\t")) sp = sp.substring(1);
            while (sp.startsWith(" ")) sp = sp.substring(1);
            if (i > 0) result += " ";
            result += level + sp + "\n";
        }
        return result;
    }
}
