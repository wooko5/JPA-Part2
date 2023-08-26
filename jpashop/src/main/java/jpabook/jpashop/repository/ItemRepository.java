package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager entityManager;

    /* 상품 등록 */
    public void save(Item item) {
        if (item.getId() == null) {
            entityManager.persist(item);
        } else {
            /**
             * merge()는 준영속성 상태의 엔티티를 영속 상태로 변경할 때 사용한다
             * 쉽게 말하자면, 저장할 Item 엔티티와 PK가 동일한 엔티티가 DB에 존재하면 해당 엔티티를 update처럼 값을 변경해준다
             * 단점 : 엔티티를 변경할 때, 어떤 속성값이 초기화 되어있지 않다면 DB에 NULL로 저장됨
             * WEB에서 더 자세하게 알아본다
             * */
            entityManager.merge(item);
        }
    }

    /* 상품 목록 조회 */
    public Item findOne(Long id) {
        return entityManager.find(Item.class, id);
    }

    /* 상품 수정 */
    public List<Item> findAll() {
        return entityManager.createQuery("SELECT a FROM Item a", Item.class)
                .getResultList();
    }

}
