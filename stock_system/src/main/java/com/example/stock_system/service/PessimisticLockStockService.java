package com.example.stock_system.service;

import com.example.stock_system.domain.Stock;
import com.example.stock_system.repository.StockRepository;
import org.springframework.transaction.annotation.Transactional;

public class PessimisticLockStockService {

    private StockRepository stockRepository;

    public PessimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Long decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);

        return stock.getQuantity();
    }

}
