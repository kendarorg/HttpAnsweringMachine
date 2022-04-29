package com.kendar.finance.api;

import com.kendar.finance.api.model.StockData;
import com.kendar.finance.utils.MimeTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RestController()
@RequestMapping("/api/stock")
public class StockController {

    @GetMapping(value ="/{stockId}", produces = MimeTypes.JSON)
    public StockData getStockData(@PathVariable String stockId) throws IOException {
        Stock stock = YahooFinance.get(stockId);
        var result = new StockData();
        result.setId(stockId);
        result.setPrice(testGet(()->stock.getQuote().getPrice()));
        result.setChangeInPercent(testGet(()->stock.getQuote().getChangeInPercent()));
        result.setPeg(testGet(()->stock.getStats().getPeg()));
        result.setDividend(testGet(()->stock.getDividend().getAnnualYieldPercent()));
        return result;
    }

    private double testGet(Supplier<BigDecimal> o) {
        try{
            return o.get().doubleValue();
        }catch (Exception ex){
            return 0.00;
        }
    }
}
