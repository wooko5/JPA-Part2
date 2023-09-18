package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.OrderStatus.CANCLE;
import static jpabook.jpashop.domain.OrderStatus.ORDER;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    /*
    Order클래스의 Member는 Lazy전략 이기 때문에 포스트맨으로 Order 전체 조회를 하면, JPA에서 관련된 Member는 프록시 객체로 가짜로 넣어둔다.
    그래서 Order 전체 조회 시, Order와 관련된 Member를 조회할 때, 순수 객체가 아닌 프록시 객체인 ByteBuddyInterceptor()를 조회하려고 하니 '500' 에러 발생
    private Member member = new ByteBuddyInterceptor();
    */
    @ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정, 연관관계의 주인으로 본다
    @JoinColumn(name = "member_id") // 어떤 칼럼을 조인칼럼으로 쓸것인가? ==> Member의 member_id를 조인칼럼으로 쓰겠다(DB 관점)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>(); // 컬렉션은 필드에서 초기화하자

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // 모든 연관관계는 지연로딩으로 설정한다
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; // 여기서는 테이블 access를 Order에 많이 하므로 1대1 연관관계의 주인을 Order로 했지만 Delivery에 놔도 된다(사람에 따라 다른 것, 답은 없다)

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문상태 [ORDER, CANCLE]

    /* 연관관계 편의 메소드 생성, 양방향 관계에서 주인쪽(FK소유)에 메소드를 만들어주는게 좋다*/
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

    /* 비즈니스 로직 - 생성 메서드, OrderItem...은 여러개를 의미한다 */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    /* 비즈니스 로직 - 주문취소 메서드 */
    public void cancle() {
        if (delivery.getDeliveryStatus() == DeliveryStatus.COMPLETED) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(CANCLE);
        /**
         * this.orderItems로 써도 되지만 생략하는 이유:
         * 이미 IDE에서 해당 orderItems로만 써도 보라색으로 색칠하기 때문에 식별하기 쉬워서 굳이 this.orderItems로 쓰지 않았다.
         * 개인 취향의 문제이지만 주로 1.강조하고 싶을 때, 2.변수 이름이 중복될 때를 제외하고는 잘 안 쓴다.
         */
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel(); // orderItem의 재고 수량을 원복하는 메소드 처리
        }
    }

    /* 조회 로직 - 전체 주문 가격을 조회하는 메서드 */
    public int getTotalPrice() {
//        int totalPrice = 0;
//        for (OrderItem orderItem : orderItems) {
//            totalPrice += orderItem.getTotalPrice();
//        }
//        return totalPrice;
        /* stream 방식으로 하는 방법도 존재 */
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}