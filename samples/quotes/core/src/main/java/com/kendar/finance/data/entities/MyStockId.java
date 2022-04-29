package com.kendar.finance.data.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

public class MyStockId implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Id
    private Long userId;

    public MyStockId(){

    }

    public MyStockId(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "MyStockId{" +
                "id=" + id +
                ", userId=" + userId +
                '}';
    }
}
