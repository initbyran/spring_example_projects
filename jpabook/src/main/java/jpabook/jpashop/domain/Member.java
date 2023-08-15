package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
// setter 사용지양 : 변경 지점이 명확하도록
// 변경을 위한 비즈니스 메서드르 별도로 제공해야한다
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded // @Embedded, @Embeddable 둘 중 하나만 사용해도 되지만 보통 둘 다 써준다
    private Address address;

    @JsonIgnore // 반환시 제외됨
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
