package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ItemServiceTest {

    @Autowired
    EntityManager entityManager;
    @Test
    @DisplayName("")
    public void updateTest() throws Exception {
        //given
        Book book = entityManager.find(Book.class, 1L);

        /**
         * setter를 사용해서 변경하는 순간, 변경 감지(Dirty Checking)가 발생해서
         * flush()가 실행되면 JPA가 알아서 DB에 수정 SQL문을 보냄
         * 예를 들어, Order의 cancle()에서 'this.setStatus(CANCLE);'가 처리되면 '변경 감지'가 발생하고
         * 그로인해서 어느 시점에 JPA의 flush()가 실행되면 DB에 SQL문을 보낸다
         */
        book.setName("랜덤돌리기책");

        //then
    }
}