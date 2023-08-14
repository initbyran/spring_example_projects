package jpabook.jpashop.respository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;
    public void save(Order order) {
        em.persist(order);
    }
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 검색 : 동적 쿼리 필요
//     public List<Order> findAll(OrderSearch orderSearch) {
//        em.createQuery("select o from Order o join o.member m" +
//                " where o.status = :status "+
//                " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(1000)
//                .getResultList();
//
//     }


}
