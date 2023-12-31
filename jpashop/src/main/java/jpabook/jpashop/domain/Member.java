package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id") // DB에 표현될 칼럼명
    private Long id;

    @NotBlank(message = "회원의 이름은 공백일 수 없습니다.")
    private String name;

    @Embedded
    private Address address;

    /**
     * Order 클래스의 private Member member;에서 따온 것, JoinColum 방식(DB 칼럼명을 씀)과 헷갈리지 않도록 조심!
     * mappedBy는 양방향 관계에서 주인이 아닌 객체에서 사용한다.(읽기만 가능)
     * 즉 Member는 Order를 수정할 수 없고 조회만 가능하지만, Order는 Member를 가지고 있기에 조회 수정 모두 가능
     */
    @JsonIgnore //불필요한 '주문' 정보를 JSON으로 보여주지 않기 위한 어노테이션
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>(); // 컬렉션은 필드에서 초기화 하자.

    /* 일대다 연관관계의 일측(Member)에서 연관관계를 지정할 때 기존 연관관계는 끊어주어야 한다. */
//    public void setOrder(Order order) {
//        this.orders.add(order);
//        if (order.getMember() != this) {
//            order.setMember(this);
//        }
//    }
}