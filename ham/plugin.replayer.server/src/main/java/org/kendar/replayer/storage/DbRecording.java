package org.kendar.replayer.storage;


import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.HashMap;

@Component

@Entity
@Table(name="REPLAYER_RECORDING")
public class DbRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="description")
    private String description;

}
