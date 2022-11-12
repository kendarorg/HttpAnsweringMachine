package org.kendar.replayer.storage;


import org.springframework.stereotype.Component;

import javax.persistence.*;

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

    public String getDescripton() {
        return descripton;
    }

    public void setDescripton(String descripton) {
        this.descripton = descripton;
    }

    @Column(name="description")
    private String descripton;
}
