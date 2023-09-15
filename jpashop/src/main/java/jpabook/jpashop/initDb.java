package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * 초기 샘플데이터를 입력하기 위한 클래스
 * @PostConstruct를 통해서 데이터를 생성
 *
 * Oh (Member)
 *  - JPA Part1
 *  - JPA Part2
 * Eom (Member)
 *  - Spring Part1
 *  - Spring Part2
 */
@Component
@RequiredArgsConstructor
public class initDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }


    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager entityManager;

        public void dbInit1() {
            Member member = createInitialMember("Oh", "Seoul", "yeonhee-ro", "03171");
            entityManager.persist(member);

            Book book1 = createInitialBook("JPA Part1", 10000, 100);
            entityManager.persist(book1);

            Book book2 = createInitialBook("JPA Part2", 20000, 100);
            entityManager.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 1);

            Delivery delivery = createInitialDelivery(member); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            entityManager.persist(order);
        }

        public void dbInit2() {
            Member member = createInitialMember("Eom", "Busan", "oncheon-ro", "77717");
            entityManager.persist(member);

            Book book1 = createInitialBook("Spring Part1", 20000, 200);
            entityManager.persist(book1);

            Book book2 = createInitialBook("Spring Part2", 40000, 300);
            entityManager.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createInitialDelivery(member); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            entityManager.persist(order);
        }

        private Member createInitialMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        private Book createInitialBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Delivery createInitialDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress()); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함 ==> 교육이니깐!!
            return delivery;
        }
    }
}
