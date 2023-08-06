package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    // enum type ; ordinal(쓰지말것), string
    // ordinal : 숫자로 -> 새로운 타입이 추가되면 문제가 발생
    // string
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus; //[ready, comp]

}
