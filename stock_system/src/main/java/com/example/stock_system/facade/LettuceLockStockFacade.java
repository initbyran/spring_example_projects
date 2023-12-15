package com.example.stock_system.facade;

import org.springframework.stereotype.Component;
import com.example.stock_system.repository.RedisLockRepository;
import com.example.stock_system.service.StockService;

@Component
public class LettuceLockStockFacade {

    private RedisLockRepository redisLockRepository;

    private StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long key, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(key)) {
            Thread.sleep(100); // redis 부하 줄여줌
        }

        try {
            stockService.decrease(key, quantity);
        } finally {
            redisLockRepository.unlock(key); // lock 해제
        }
    }
}
