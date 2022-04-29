package com.kendar.finance.api;

import com.kendar.finance.api.model.StockData;
import com.kendar.finance.data.MyStocksRepository;
import com.kendar.finance.utils.MimeTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController()
@RequestMapping("/api/mystock")
public class MyStockController {
    private final MyStocksRepository myStocksRepository;

    public MyStockController(MyStocksRepository myStocksRepository){

        this.myStocksRepository = myStocksRepository;
    }

    @GetMapping(value ="", produces = MimeTypes.JSON)
    public StockData getMyStocks(@PathVariable String stockId) throws IOException {
        return null;
    }
}
