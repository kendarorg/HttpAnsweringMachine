package org.kendar.replayer.engine.db;


import org.kendar.janus.cmd.interfaces.JdbcCommand;

import java.util.ArrayList;
import java.util.List;

public class DbTreeItem {

    private final List<DbRow> targets = new ArrayList<>();
    private final List<DbTreeItem> children = new ArrayList<>();
    private String initiator;
    private DbTreeItem parent;

    public DbTreeItem() {
    }

    public String getInitiator() {
        return initiator;
    }

    public void addTarget(DbRow target) {
        var httpRequest = target.getRow().getRequest();
        this.initiator = httpRequest.getPathParameter("targetType");
        targets.add(target);

    }

    public List<DbTreeItem> getChildren() {
        return children;
    }

    public void addChild(DbTreeItem item) {
        item.setParent(this);
        children.add(item);
    }

    public DbTreeItem getParent() {
        return parent;
    }

    public void setParent(DbTreeItem parent) {
        this.parent = parent;
    }

    public List<DbRow> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        return toString(0);

    }

    private String toString(int level) {
        StringBuilder pad = new StringBuilder();
        for (var i = 0; i < level; i++) {
            pad.append("  ");
        }

        StringBuilder result = new StringBuilder();
        result.append(pad).append("{\n");
        result.append(pad).append(" ").append(initiator).append("\n");

        if (targets.size() > 0) {
            result.append(pad).append(" targets:[\n");
            for (int i = 0; i < targets.size(); i++) {
                DbRow target = targets.get(i);
                result.append(addPad(target.getRequest(), pad + "  "));
                if (i < (targets.size() - 1)) {
                    result.append("\n");
                }
            }
            result.append(pad).append(" ],\n");
        }
        if (children.size() > 0) {
            result.append(pad).append(" children:[\n");
            for (int i = 0; i < children.size(); i++) {
                DbTreeItem child = children.get(i);
                result.append(child.toString(level + 1));
                if (i < (children.size() - 1)) {
                    result.append("\n");
                }
            }
            result.append(pad).append(" ]\n");
        }
        result.append(pad).append("}\n");

        return result.toString();
    }

    private String addPad(JdbcCommand request, String level) {
        var splitted = request.toString().split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < splitted.length; i++) {
            String sp = splitted[i];
            while (sp.startsWith("\t")) sp = sp.substring(1);
            while (sp.startsWith(" ")) sp = sp.substring(1);
            if (i > 0) result.append(" ");
            result.append(level).append(sp).append("\n");
        }
        return result.toString();
    }
}
