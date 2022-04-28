// 
// Decompiled by Procyon v0.5.36
// 

package com.wbsoftwareconsutlancy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;

@RestController()
@RequestMapping("/stock-quote-last-price")
class StockQuoteLastPriceHandler
{
    @GetMapping(value ="/{stockId}",produces = "application/json")
    public double handle(@PathVariable String stockId) throws IOException {
        Stock stock = YahooFinance.get(stockId);

        var price = stock.getQuote().getPrice();
        var change = stock.getQuote().getChangeInPercent();
        var peg = stock.getStats().getPeg();
        var dividend = stock.getDividend().getAnnualYieldPercent();
        return price.doubleValue();
    }

    @GetMapping(value ="",produces = "application/json")
    public double handle() throws IOException {
        return handle("AAPL");
}
}
