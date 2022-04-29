package com.kendar.finance.data.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@IdClass(MyStockId.class)
public class MyStock {
    @Id
    @GeneratedValue
    private Long id;
    @Id
    private Long userId;

    private BigDecimal price = new BigDecimal("0.00");
    private Integer quantity = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyStock myStock = (MyStock) o;
        return Objects.equals(id, myStock.id) && Objects.equals(userId, myStock.userId) && Objects.equals(price, myStock.price) && Objects.equals(quantity, myStock.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, price, quantity);
    }

    @Override
    public String toString() {
        return "MyStock{" +
                "id=" + id +
                ", userId=" + userId +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
