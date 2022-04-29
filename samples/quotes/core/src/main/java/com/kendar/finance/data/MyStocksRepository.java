package com.kendar.finance.data;

import com.kendar.finance.data.entities.MyStock;
import com.kendar.finance.data.entities.MyStockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyStocksRepository extends JpaRepository<MyStock, MyStockId> {
}
