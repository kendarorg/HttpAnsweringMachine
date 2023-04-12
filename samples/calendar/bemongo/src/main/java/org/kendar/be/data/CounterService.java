package org.kendar.be.data;

import org.kendar.be.data.entities.Counter;
import org.springframework.stereotype.Component;

@Component
public class CounterService {
    private CountersRepository countersRepository;

    public CounterService(CountersRepository countersRepository){

        this.countersRepository = countersRepository;
    }
    public long getNextValue(String table){
        var counter = countersRepository.findByCollection(table);
        if(counter==null){
            counter = new Counter();
            counter.setTable(table);
            counter.setCounter(1);
            countersRepository.save(counter);
        }else{
            counter.setCounter(counter.getCounter()+1);
            countersRepository.save(counter);
        }
        return counter.getCounter();
    }
}
