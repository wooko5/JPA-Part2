package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    /**
     * RDB에서는 양방향 다대다 연관관계를 표현하는게 힘들기 때문에
     * category_item 이라는 중간 매핑 테이블을 만들기 위해 선언함
     * category_item 매핑 테이블에 칼럼은 FK로 가져온 category_id, item_id만 존재
     *
     * 실무에서 양방향 다대다 연관관계를 안 쓰는 이유: 중간 매핑 테이블에 칼럼을 추가하는 등의 일이 불가능함
     * 예를 들어, 개발하다보면 생성/수정일시 같은 칼럼을 넣고싷어도 넣을 수가 없음
     */
    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Item> items = new ArrayList<>(); // 컬렉션은 필드에서 초기화하자

    /**
     * parent와 child는 Category 자기 자신에게 양방향 관계를 만든 것이다.
     */
    @ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>(); // 컬렉션은 필드에서 초기화하자

    /* 연관관계 편의 메소드 생성 */
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }
}
