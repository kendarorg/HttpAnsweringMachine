package org.kendar.servers.logging;


import javax.persistence.*;
import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;


@Component
@Entity
@Table(name="LOGS_DATA")
public class LoggingDataTable implements DbTable {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "content")
    private String content;
}
