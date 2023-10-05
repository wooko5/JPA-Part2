package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    /* 저장로직은 readOnly = false이기에 */
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
     * 트랜잭션 안에서 엔티티를 다시 조회, 변경할 값을 선택하고
     * 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)가 동작해서 데이터베이스에 UPDATE SQL 실행
     * ==============================================================================
     * 코드가 길어지면 어디서 필드가 변경됐는지 파악하기 힘들기 때문에
     * setter를 이용하는 것보다 foundItem.change(price, name, stockQuantity);처럼
     * 비즈니스 로직을 엔티티에 생성하는 것이 DDD 유지/보수 측면에서 좋다
     */
    @Transactional
    public Item updateItem(Long itemId, String name, int price, int stockQuantity){
        //TODO: Book, Album, Movie에 따라서 수정할 수 있는 항목이 추가되는데 이 부분에 대해서 ItemController에서 할지 아님 다른 방법을 쓸지 정하기
        Item foundItem = itemRepository.findOne(itemId);
        foundItem.change(price, name, stockQuantity);
        return foundItem; //itemRepository.save(foundItem); 해당 코드를 사용하지 않아도 foundItem는 영속 상태의 엔티티라서 save()를 쓰지않아도 된다
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findItem(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
