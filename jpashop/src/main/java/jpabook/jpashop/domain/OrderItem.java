package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격

    private int count; // 주문 수량

//    protected OrderItem(){
//        /*
//        OrderItem의 인스턴스를 생성할 때 '주문상품 생성 메서드'을 강제하게 위해서 만든 생성자
//        protected로 하면 'OrderItem orderItem = new OrderItem()'을 못 쓴다.
//        @NoArgsConstructor(access = AccessLevel.PROTECTED)와 같은 코드다.
//        */
//    }

    /* 주문상품 생성 메서드 */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        item.removeStock(count); // 넘어온 count만큼 item에서 재고(stockQuantity)를 제거해줌
        return orderItem;
    }

    /* 비즈니스 로직 - 재고 수량을 원복해주는 메서드 */
    public void cancel() {
        getItem().addStock(count); // this.getItem().addStock(count);으로 작성할 수 있지만 헷갈릴 getter가 없어서 간단하게 작성
    }

    /* 비즈니스 로직 - 주문의 전체 가격을 반환하는 메서드 */
    public int getTotalPrice() {
        return getOrderPrice() * getCount(); // 주문가격 * 수량 == 주문 전체 가격
    }
}
