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
     * 둘 이상의 Thread가 공유 데이터에 액세스할 수 있고,
     * 동시에 변경을 하려고 할 때 발생(누락, 중복 등)
     * => 하나의 thread가 작업 완료된 후 접근할 수 있도록 해야함.
     * => synchronized 사용
     *
     * 두번째 동시성 테스트 : 실패
     * 이유 : transactional
     * 래핑한 클래스를 새로 만들어서 진행.
     * 트랜잭션 종료 시점에 데이터베이스에 업데이트를 함.
     * 이 때, 실제 데이터베이스가 업데이트 되기 전에
     * 다른 thread가 동일 method를 호출할 수 있음.
     * => transactional annotation 주석 처리 후 synchronized만 사용.
     *
     * synchronized의 문제점?
     * 각 프로세스내에서만 보장, 서버가 여러대일 경우 동시 접근 문제 발생.
     * => 레이스 컨디션 발생.
     *
     * Mysql 을 활용한 다양한 방법
     *
     * Pessimistic Lock
     * 실제로 데이터에 Lock 을 걸어서 정합성을 맞추는 방법입니다.
     * exclusive lock 을 걸게되며 다른 트랜잭션에서는 lock 이 해제되기전에 데이터를 가져갈 수 없게됩니다.
     * 데드락이 걸릴 수 있기때문에 주의하여 사용하여야 합니다.
     *
     *
     * Optimistic Lock
     * 실제로 Lock 을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법입니다.
     * 먼저 데이터를 읽은 후에 update 를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트 합니다.
     * 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은후에 작업을 수행해야 합니다.
     *
     *
     * Named Lock
     * 이름을 가진 metadata locking 입니다.
     * 이름을 가진 lock 을 획득한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없도록 합니다.
     * 주의할점으로는 transaction 이 종료될 때 lock 이 자동으로 해제되지 않습니다.
     * 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제됩니다.
     * (get-lock, release-lock)
     * 분산락에 주로 사용
     *
     * pessimistic lock과의 차이점
     *  => 로우나 테이블 단위가 아니라 메타 데이터의 락깅을 하는 것
     *  => 타임아웃 손쉽게 구현
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