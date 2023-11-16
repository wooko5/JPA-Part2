package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * OrderService에서 order() 메서드를 만든 이유
     * OrderService의 메소드에는 Controller의 메소드와 다르게 `@Transactional` 어노테이션이 존재한다
     * @Transactional이 선언되어 있다면 엔티티를 조회하거나 생성할 때, 영속성 컨텍스트 안에서 영속성을 유지하기 때문에
     * 해당 엔티티를 수정하면 `변경 감지(Dirty Checking)` 발생
     * TIP) 파라미터로 엔티티를 받으면 repository에서 가져온 영속성의 엔티티가 아니기에 식별자(id, name..)로 받는 것을 추천
     */
    //주문하기
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress()); // 실제로는 멤버의 주소가 아니라 도착지 정보를 설정해야하지만 간단하게 하기위해 참작


        /**
         * OrderItem orderItem = new OrderItem()처럼 생성자로 생성하지 못 하게
         * OrderItem.createOrderItem()로 생성 제한
         * 항상 코드는 제약되는 형식으로 짜야 유지보수가 편하다
         * 예를 들어, 주문상품 인스턴스 생성을 생성자로도 만들고, 정적 팩토리 메소드로도 하게 된다면 대규모 프로젝트에서
         * 코드가 하나의 일관된 표준없이 작성될 가능성이 커진다.
         */
        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        /**
         * delivery, orderItem을 각각 persist() 해주지 않고, orderRepository.save(order)만 해줘도 되는걸까?
         * ==> Yes, Order가 Delivery, OrderItem을 'cascade = CascadeType.ALL'로 영속성 전이를 해주기 때문에
         * entityManager.persist(order)만 해도 JPA가 알아서 처리해준다.
         *
         * 참조하는 엔티티가 하나밖에 없는 Private Owner인 경우에 'cascade'옵션을 쓰는 것을 추천.
         * 예를 들어, Delivery와 OrderItem을 참조하는 엔티티는 Order만 존재한다.
         * (물론 OrderItem가 Item을 참조하지만 OrderItem을 참조하는 것은 Order만 존재한다)
         * 만약 수많은 곳에서 OrderItem, Delivery을 참조하면 'cascade'옵션을 안 쓰는 것이 좋다.
         */
        //주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 조회
        Order order = orderRepository.findOne(orderId);

        //주문 취소
        order.cancle();
        /**
         * 기존의 JDBC 템플릿이나 MyBatis에서는 엔티티의 칼럼을 수정하면
         * 수정 SQL문을 그때마다 수행해야 했지만, JPA에서는 DirtyChecking을 통해
         * 변경된 칼럼에 대해서 알아서 DB에 반영해준다.(persist => flush)
         */
    }

    /**
     * 참고: 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다.
     * 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
     * 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을
     * 도메인 모델 패턴(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다.
     * 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을
     * 트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.
     */

    //주문 검색
    public List<Order> findOrders(OrderSearch orderSearch){
        return orderRepository.findAllByQueryDsl(orderSearch);
    }
}
