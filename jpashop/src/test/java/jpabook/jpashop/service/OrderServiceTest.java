package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("반지의제왕", 40000, 10);
        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order foundOrder = orderRepository.findOne(orderId);
        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, foundOrder.getStatus()); // (메시지, 예상값, 실제값)
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, foundOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다", 40000 * orderCount, foundOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야한다", 8, book.getStockQuantity()); // 10개 책 상품 중에 2개를 구매해서 8개 재고
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("반지의제왕", 40000, 10);
        int orderCount = 20;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);

        //then
        fail("재고 수량 예외가 발생해야 한다.");
    }


    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("반지의제왕", 40000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order foundOrder = orderRepository.findOne(orderId);
        assertEquals("주문 취소 시 상태는 CANCLE이다.", OrderStatus.CANCLE, foundOrder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야한다.", 10, book.getStockQuantity());
    }

    @Test
    public void 스트링빌더_테스트(){
        StringBuilder query = new StringBuilder("SELECT o FROM Order o join o.member m");
        query.append(" + 1");
        assertEquals(query.toString(), "SELECT o FROM Order o join o.member m + 1"); // toString()가 없으면 StringBuilder라서 테스트 실패
    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        entityManager.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원A");
        member.setAddress(new Address("서울 서대문구", "아라내로 380-1", "03717"));
        entityManager.persist(member);
        return member;
    }
}