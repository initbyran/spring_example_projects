package com.example.stock_system.service;

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
@SpringBootTest
class StockServiceTest {


    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    // 재고 초기값 부여
    @BeforeEach
    public void before(){
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    // 재고 초기화
    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소(){
        stockService.decrease(1L, 1L);

        // 100 - 1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    /**
     *
     * 첫번째 동시성 테스트 : 실패
     * 이유 : 레이스 컨디션
     *
     * 레이스 컨디션이란 ?
     * 둘 이상의 Thread가 공유 데이터에 액세스할 수 있고,
     * 동시에 변경을 하려고 할 때 발생(누락, 중복 등)
     * => 하나의 thread가 작업 완료된 후 접근할 수 있도록 해야함.
     * => synchrosized 사용
     *
     */
    @Test
    public void 동시요청_100개() throws InterruptedException {

        // 멀티 스레드 활용
        int threadCount = 100;
        /**
         *
         * ExecutorService란?
         * 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 java의 API
         *
         * CountDownLatch란?
         * 다른 Thread에서 수행 중인 작업이 완료될 때까지 대기할 수 있도록 도와주는 클래스
         *
         */
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);

        // 100개 반복 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
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