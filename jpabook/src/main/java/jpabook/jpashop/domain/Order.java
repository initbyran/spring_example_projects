package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter // setter는 꼭 필요한 경우만
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

}
