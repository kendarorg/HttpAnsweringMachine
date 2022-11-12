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
    private HashMap<String, String> variables;
    private HashMap<String, String> preScript;
    private HashMap<String, String> postScript;

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

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, String> variables) {
        this.variables = variables;
    }

    public HashMap<String, String> getPreScript() {

        return preScript;
    }

    public void setPreScript(HashMap<String, String> preScript) {
        this.preScript = preScript;
    }

    public HashMap<String, String> getPostScript() {
        return postScript;
    }

    public void setPostScript(HashMap<String, String> postScript) {
        this.postScript = postScript;
    }
}
