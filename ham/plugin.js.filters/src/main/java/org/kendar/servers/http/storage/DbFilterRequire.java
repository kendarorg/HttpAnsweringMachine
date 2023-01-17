package org.kendar.servers.http.storage;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name="JS_FILTERS_REQUIRE")
public class DbFilterRequire implements DbTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "binary")
    private boolean binary;

    @Column(name = "script_id")
    private Long scriptId;


    @Column(name = "name")
    private String name;

    @Column(name="content")
    private String content;


    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }
}
