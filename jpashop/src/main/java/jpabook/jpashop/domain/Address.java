package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * @Embeddable는 해당 클래스(엔티티)가 내장 클래스(엔티티)임을 명시한다
 * 내장 클래스(엔티티)라는건 Address를 사용하는 Member에서
 * Address를 city, street, zipcode 칼럼으로 변환해서 쓸 수 있도록
 * 명시하는 클래스(엔티티)를 의미한다 ==> 헷갈린다면 H2 콘솔을 함 봐보자
 */
@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    /**
     * setter를 제공하지 않고, 생성자를 통해서 필드에 대한 설정을 고정해버리면
     * 더 안전하게 Address를 사용할 수 있다
     */
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    /**
     * @Embeddable 타입의 클래스는
     * JPA 규약에 따라서 기본생성자(파라미터가 없는)가 public이나 protected로 선언되어야 한다.
     * JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수
     * 있도록 지원해야 하기 때문이다.
     */
    protected Address() {

    }
}