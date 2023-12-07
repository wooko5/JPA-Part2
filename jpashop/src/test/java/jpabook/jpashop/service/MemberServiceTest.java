package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.fail;

@SpringBootTest
@Transactional
public class MemberServiceTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private EntityManager entityManager; // @Rollback(false)을 안 쓰고싶다면 'entityManager.flush()'을 사용하자

    @Test
//    @Rollback(false)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("Jaeuk");

        /**
         * 실행 로그를 보면 저장 테스트인데 DB에 INSERT문을 실행하지 않는다.
         * 왜냐하면 JPA에서 em.persist(member)를 실행해도 DB에 INSERT문을 실행하는게 아니기 때문이다
         * JPA는 영속성 컨텍스트에 저장된 객체 데이터가 commit, flush가 실행되면 INSERT문을 생성하면서 DB에 전달되는 구조이기 때문이다
         *
         * commit, flush가 실행 안 되는 이유: @Transactional가 rollback이 기본적으로 true이기 때문이다
         * 그래서 해당 메소드에 @Rollback(false)로 두면 insert문이 실행된다
         */
        Long savedId = memberService.join(member); //when

        entityManager.flush();

        //then
        Assertions.assertEquals(member, memberRepository.findById(savedId).orElse(null));
    }

//    @Test(expected = IllegalStateException.class) // try-catch문을 한 줄로 없앨 수 있다. 예외처리할 것을 expected에 넣기
//    @Test
//    public void 중복_회원_예외() throws Exception {
//        //given
//        Member memberA = new Member();
//        memberA.setName("Oh");
//
//        Member memberB = new Member();
//        memberB.setName("Oh");
//
//        //when
//        memberService.join(memberA);
//
//        //then
//        assertThrows(IllegalStateException.class, () -> {
//            memberService.join(memberB);
//        });
//    }

    @Test
    public void 원본_컬렉션과_하이버네이트_컬렉션_확인() {
        Member member = new Member();
        log.info("====================START=====================");
        log.info("member = {}", member.getOrders().getClass());
        log.info("====================END=====================");
        entityManager.persist(member);
        log.info("====================START=====================");
        log.info("member = {}", member.getOrders().getClass());
        log.info("====================END=====================");
    }

    @Test
    public void 엔_플러스_1_문제_테스트() {
        Member member1 = new Member();
        Member member2 = new Member();
        Member member3 = new Member();
        member1.setName("root1");
        member2.setName("root2");
        member3.setName("root3");
        memberService.join(member1);
        memberService.join(member2);
        memberService.join(member3);

        Order order1 = new Order();
        Order order2 = new Order();
        order1.setStatus(OrderStatus.ORDER);
        order2.setStatus(OrderStatus.ORDER);
        List<Order> orderList = new ArrayList<>(Arrays.asList(order1, order2));

        orderRepository.save(order1);
        orderRepository.save(order2);

        member1.setOrders(orderList);
        member2.setOrders(orderList);
        member3.setOrders(orderList);

        entityManager.flush();

        List<Member> memberList = memberRepository.findAll();
        System.out.println("==========================================================");
        System.out.println("전체 멤버 데이터는 몇 개 일까 === " + memberList.size());
        System.out.println("==========================================================");

//        List<Order> orders = orderRepository.findAll();
//        System.out.println("==========================================================");
//        System.out.println("전체 주문 데이터는 몇 개 일까 === " + orders.size());
//        System.out.println("==========================================================");
    }


}