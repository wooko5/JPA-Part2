package jpabook.jpashop;

import jpabook.jpashop.domain.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProxyAndLazyTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DisplayName("실제 DB에서 엔티티 객체를 가져오는 테스트")
    @Test
    public void testForRealEntity() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Member member = new Member();
        member.setName("Jaeuk");
        entityManager.persist(member);
        entityManager.flush();
        entityManager.clear();

        Member foundMember = entityManager.find(Member.class, member.getId());
        Assert.assertEquals(foundMember.getClass().getName(), member.getClass().getName() + "1");

        entityTransaction.commit();
        entityManager.close();
    }

    @DisplayName("JPA가 만들어준 프록시 객체를 가져오는 테스트")
    @Test
    public void testForProxyEntity() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Member member = new Member();
        member.setName("Jaeuk");
        entityManager.persist(member);
        entityManager.flush();
        entityManager.clear();

        Member foundMember = entityManager.getReference(Member.class, member.getId());
        Assert.assertEquals(foundMember.getClass().getName(), member.getClass().getName());

        entityTransaction.commit();
        entityManager.close();
    }
}
