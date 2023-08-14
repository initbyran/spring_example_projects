package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.respository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional // 우선권
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 준영속 상태 업데이트 하는 방법 : 변경 감지 기능 사용
    // -> em.merge(Item)이랑 동일한 방식으로 작동
    // -> 데이터베이스 안전성을 위해 병합은 사용하지 않는다

    /**
     * 수정 : 트랜잭션이 있는 서비스 계층에 식별자와 변경할 데이터를 명확하게 전달(DTO)
     * 1. transaction이 있는 service 계층에서 영속 상태의 entity 조회
     * 2. entity 직접 변경
     * 3. transaction commit 시점에 dirty checking 실행됨
     */
    @Transactional
    public Item updateItem(Long itemId, String name, int price, int stockQuantity){
        Item findItem = itemRepository.findOne(itemId);
//      setter사용 지양 : findItem.change(price, name, stockQuantity);
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
        // 아무것도 해 줄 필요가 없다
        // -> findOne을 함으로써 영속 상태로 관리가 된다
        // -> commit시 flush되면서 자동 update query 실행
        return findItem;
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
