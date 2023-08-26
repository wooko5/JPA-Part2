package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InheritanceType.SINGLE_TABLE : 한 테이블에 모든 데이터를 넣는 것(우리가 쓸 전략)
 * InheritanceType.JOINED : 가장 RDB답게 정규화된 스타일로 하는것(수정중)
 * InheritanceType.TABLE_PER_CLASS : Item을 상속받은 Album, Book, Movie 세 개를 테이블로 만드는 방법
 * 모두 장단점이 있으므로 팀에 맞는 방식을 쓰자
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>(); // 컬렉션은 필드에서 초기화하자

    @OneToMany(mappedBy = "item")
    private List<OrderItem> orderItems = new ArrayList<>(); // 컬렉션은 필드에서 초기화하자

    /* 비즈니스 로직 - 상품 재고 추가 */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /* 비즈니스 로직 - 상품 재고 감소 */
    public void removeStock(int quantity) {
        int stock = this.stockQuantity - quantity;
        if (stock < 0) {
            throw new NotEnoughStockException("Not Enough Energy(stock quantity)");
        }
        this.stockQuantity = stock;
    }
}
