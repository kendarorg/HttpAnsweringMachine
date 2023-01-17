package org.kendar.servers.http.storage;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name="JS_FILTERS")
public class DbFilter implements DbTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "matcher")
    private String matcher;

    @Column(name = "source")
    private String source;

    @Column(name = "phase")
    private String phase;

    @Column(name = "priority")
    private int priority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatcher() {
        return matcher;
    }

    public void setMatcher(String matcher) {
        this.matcher = matcher;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
