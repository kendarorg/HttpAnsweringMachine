package com.kendar.finance.api;

import com.kendar.finance.utils.MimeTypes;
import org.springframework.web.bind.annotation.*;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/stocks")
public class AggregatedStocksController {

    @PostMapping(value ="/prices", produces = MimeTypes.JSON, consumes = MimeTypes.JSON)
    public Map<String,BigDecimal> getStockData(@RequestBody List<String> stockIds) throws IOException {
        var result = new HashMap<String, BigDecimal>();
        if(stockIds.size()==0) return result;
        var stocks = YahooFinance.get(stockIds.toArray(new String[]{})); // single request
        for(var stock:stocks.entrySet()){
            result.put(stock.getKey(),stock.getValue().getQuote().getPrice());
        }
        return result;
    }
}
