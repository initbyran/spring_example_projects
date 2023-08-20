package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter // setter는 꼭 필요한 경우만
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 여기저기서 직접 생성자를 호출해서 set을 사용하지 못하게함 -> 하나의 생성메소드로 관리할 수 있음
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 연관관계의 주인을 정해야함 (변경이 되는 주체)
    // -> fk가 가까운 곳을 주인으로!
    // @XtoOne : eager가 default -> N+1문제 때문에 지연로딩으로 변경해주어야함
    // @XToMany : lazy가 default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // cascade
    // @BatchSize(size = 1000)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // cascade
    // access를 많이 하는 곳에 fk를 둔다 : order > delivery
    // 연관관계의 주인은 fk와 가까운 곳
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // annotation필요없이 hibernate가 자동으로 처리 해줌
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //[order, cancel]

    //==연관관계 편의 메서드(양방향)==//
    // 위치 ; 실질적으로 컨트롤하는 주체

    // 도메인 모델 패턴 (서비스 - 위임의 역할 / 엔티티 - 비즈니스 로직)
    // 트랜잭션 스크립트 패턴 (서비스 - 비즈니스 로직 )
    // 뭐가 더 유지보수가 용이할지를 고민해서 선택 : JPA에서는 도메인 모델 패턴이 많이 쓰임
    // 한 프로젝트안에서 두가지 패턴이 혼용되기도 함

    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery,
                                    OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    /** 주문 취소 */
    // dirty checking - update query를 직접 작성하지 않아도된다
    public void cancel() {

        if (delivery.getDeliveryStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);

        // order item 각각도 취소해줘야함
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /** 전체 주문 가격 조회 */
    public int getTotalPrice() {
        int totalPrice = 0;

        // order item 각각의 가격 * 수량 한 것을 가져와야함
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
//      한줄로 바꾸고 싶으면 : return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}


