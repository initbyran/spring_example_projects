package com.example.stock_system.service;

import com.example.stock_system.domain.Stock;
import com.example.stock_system.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    public void decrease(Long id, Long quantity){
        // Stock 조회 -> 재고 감소 -> 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
