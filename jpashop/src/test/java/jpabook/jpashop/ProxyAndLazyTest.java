package jpabook.jpashop;

import jpabook.jpashop.domain.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@SpringBootTest
public class ProxyAndLazyTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

//    @DisplayName("실제 DB에서 엔티티 객체를 가져오는 테스트")
//    @Test
//    public void testForRealEntity() {
//
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        EntityTransaction entityTransaction = entityManager.getTransaction();
//
//        entityTransaction.begin();
//
//        Member member = new Member();
//        member.setName("Jaeuk");
//        entityManager.persist(member);
//        entityManager.flush();
//        entityManager.clear();
//
//        Member foundMember = entityManager.find(Member.class, member.getId());
//        Assertions.assertEquals(foundMember.getClass().getName(), member.getClass().getName() + "1");
//
//        entityTransaction.commit();
//        entityManager.close();
//    }
//
//    @DisplayName("JPA가 만들어준 프록시 객체를 가져오는 테스트")
//    @Test
//    public void testForProxyEntity() {
//
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        EntityTransaction entityTransaction = entityManager.getTransaction();
//
//        entityTransaction.begin();
//
//        Member member = new Member();
//        member.setName("Jaeuk");
//        entityManager.persist(member);
//        entityManager.flush();
//        entityManager.clear();
//
//        Member foundMember = entityManager.getReference(Member.class, member.getId());
//        Assertions.assertEquals(foundMember.getClass().getName(), member.getClass().getName());
//
//        entityTransaction.commit();
//        entityManager.close();
//    }
}
