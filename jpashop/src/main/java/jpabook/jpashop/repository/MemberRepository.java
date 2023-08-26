package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class MemberRepository {

    /**
     * Spring이 EntityManager를 생성해서 의존성주입(DI)를 해준다.
     * 만약 스프링에서 DI를 해주지 않는다면 개발자가 직접 EntityManagerFactory에서
     * EntityManager를 따로 생성해서 사용해야 하기에 매우 복잡하다
     *
     * 예시:
     * @PersistenceUnit private EntityManagerFactory emf;
     */
    @PersistenceContext
    private EntityManager entityManager;

    /* 기존에 쓰던 위의 코드를 @RequiredArgsConstructor를 추가하면 간단하게 EntityManager를 주입할 수 있다 */
//    private final EntityManager entityManager;

    public void save(Member member) {
        entityManager.persist(member);
    }

    public Member findOne(Long id) {
        return entityManager.find(Member.class, id);
    }

    /**
     * JPQL과 SQL의 차이점:
     * SQL은 테이블을 대상으로한 쿼리지만,
     * JPQL은 엔티티(객체)를 대상으로 하는 쿼리다.
     */
    public List<Member> findAll() {
        return entityManager.createQuery("SELECT m FROM Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String username) {
        return entityManager.createQuery("SELECT m FROM Member m WHERE m.name = :name", Member.class)
                .setParameter("name", username)
                .getResultList();
    }
}
