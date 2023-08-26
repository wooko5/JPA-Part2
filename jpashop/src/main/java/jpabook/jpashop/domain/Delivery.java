package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id") // DB에 표현될 칼럼명
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정한다
    private Order order;

    @Embedded
    private Address address;

    /**
     * EnumType.ORDINAL : 타입을 숫자로 표현함 예를 들어, READY, COMPLETED는 0과 1로 표현 ==> 새로운 타입이 추가되면 기존의 표현이 바뀜
     * READY, X, COMPLETED ==> 0, 1, 2로 됨 그래서 안전하게 가려면 String 방식 추천
     * 기본이 EnumType.ORDINAL이니깐 꼭 String으로 설정해주자
     *
     * EnumType.STRING : 타입을 문자열로 표현함 ==> READY, COMPLETED ==> "READY", "COMPLETED"
     */
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
}
