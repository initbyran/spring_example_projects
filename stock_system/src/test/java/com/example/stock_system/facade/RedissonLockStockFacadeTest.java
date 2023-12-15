package com.example.stock_system.facade;

import static org.junit.jupiter.api.Assertions.*;

import com.example.stock_system.domain.Stock;
import com.example.stock_system.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redisson
 *
 * - pub sub
 * : 채널을 하나 만들고 락을 점유 중인 스레드가 락 획득하려고 대기중인 스레드에게 해제를 알려주면
 * 안내를 받은 스레드가 락 획득 시도를 하는 방식
 * - 락 획득 재시도를 기본으로 제공한다.
 * - pub-sub 방식으로 구현이 되어있기 때문에 lettuce 와 비교했을 때 redis 에 부하가 덜 간다.
 * - 별도의 라이브러리를 사용해야한다.
 * - lock 을 라이브러리 차원에서 제공해주기 떄문에 사용법을 공부해야 한다.
 *
 * 실무에서?
 * 재시도가 필요하지 않은 lock 은 lettuce 활용
 * 재시도가 필요한 경우에는 redisson 를 활용
 *
 */

@SpringBootTest
class RedissonLockStockFacadeTest {

    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void insert() {
        Stock stock = new Stock(1L, 100L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void delete() {
        stockRepository.deleteAll();
    }

    @Test
    public void 동시에_100개의요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockStockFacade.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - (100 * 1) = 0
        assertEquals(0, stock.getQuantity());
    }
}