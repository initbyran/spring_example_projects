package com.example.stock_system.service;

import com.example.stock_system.domain.Stock;
import com.example.stock_system.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    /**
     * propagation?
     * => 해당 로직의 트랜잭션이 기존의 트랜잭션에 어떻게 통합되거나
     * 분리되어야 하는지 전파 방식 설정
     *
     * propagation.REQUIRES_NEW?
     * => 메소드 호출시 항상 새로운 트랜잭션 시작
     * (이미 진행 중인 것은 일시 정지)
     *
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void decrease(Long id, Long quantity){
        // Stock 조회 -> 재고 감소 -> 갱신 값 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
