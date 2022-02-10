package org.kendar.replayer.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpecialStringBuilder {
    private List<String> data = new ArrayList<>();
    private int length =0;

    public SpecialStringBuilder add(String inputString){
        var toAdd = String.format("%1$" + length + "s", inputString).replace(' ', '\t');
        this.data.add(toAdd);
        return this;
    }

    public SpecialStringBuilder add(){
        this.data.add("");
        return this;
    }

    public SpecialStringBuilder tab(Consumer<SpecialStringBuilder> specialStringBuilderConsumer){
        length++;
        specialStringBuilderConsumer.accept(this);
        length--;
        return this;
    }

    public String build(){
        if(length!=0) throw new RuntimeException("Missing tab!");
        return String.join("\r\n",data);
    }

    public String build(String cr){
        if(length!=0) throw new RuntimeException("Missing tab!");
        return String.join(cr,data);
    }
}
