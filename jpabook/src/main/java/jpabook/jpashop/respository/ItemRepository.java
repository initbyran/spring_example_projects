package jpabook.jpashop.respository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            // 준영속 상태 업데이트 하는 방법 : 병합 사용 (실무에서 사용하지 말 것)
            // -> 영속 상태로 변경
            // 주의 : 변경 감지 기능은 원하는 속성만 선택해서 변경하지만
            //       병합은 전체를 변경하므로 null값이 들어오는 경우 null값으로 변경되어버림
            em.merge(item);
        }
    }
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
