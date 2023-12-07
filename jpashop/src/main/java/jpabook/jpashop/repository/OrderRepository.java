package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QOrder.*;

@Repository
public class OrderRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }


    public void save(Order order) {
        entityManager.persist(order);
    }

    public Order findOne(Long id) {
        return entityManager.find(Order.class, id);
    }

    public void saveAll(List<Order> orderList) {
        for (Order order : orderList) {
            entityManager.persist(order);
        }
    }

    /**
     * JPQL(동적쿼리 X)
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        //해당 방식으로 하면 status, name이 null일 경우 동적쿼리가 작동하지 않는다
        return entityManager.createQuery("SELECT o FROM Order o JOIN o.member m" +
                        " WHERE o.status = :status" +
                        " AND m.name LIKE :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000) // 최대 1000건
                .getResultList();
    }

    /**
     * JPQL(동적쿼리)
     * 너무 길고, 복잡해서 실무에서 잘 쓰지 않는다
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        StringBuilder jpql = new StringBuilder("SELECT o FROM Order o join o.member m");
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");

            }
            jpql.append(" o.status = :status");
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" m.name LIKE :name");
        }

        TypedQuery<Order> query = entityManager.createQuery(jpql.toString(), Order.class).setMaxResults(1000); //최대 1000건

        if (orderSearch.getOrderStatus() != null) {
            query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList(); // JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있다!!!
    }

    /**
     * JPA Criteria(동적쿼리)
     * 아까보다 그나마 나은 방법이지만 QueryDSL보다는 어렵고, 실무에서 잘 쓰지 않는다.
     * 유지/보수가 제로에 가깝다. 왜냐하면 보자마자 어떤 Query인지 감이 안 오기 때문
     */
    public List<Order> findALLByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> orderRoot = criteriaQuery.from(Order.class);
        Join<Order, Member> memberJoin = orderRoot.join("member", JoinType.INNER); //회원과 조인

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = criteriaBuilder.equal(orderRoot.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = criteriaBuilder.like(memberJoin.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        criteriaQuery.where(criteriaBuilder.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = entityManager.createQuery(criteriaQuery).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAllByQueryDsl(OrderSearch orderSearch) {

        return jpaQueryFactory
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEqual(orderSearch.getOrderStatus()), nameLike(orderSearch, member))
//                .where(order.status.eq(orderSearch.getOrderStatus())) // 둘은 같은 조건
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(OrderSearch orderSearch, QMember member) {
        if (!StringUtils.hasText(orderSearch.getMemberName())) {
            return null;
        }
        return member.name.like(orderSearch.getMemberName());
    }

    private BooleanExpression statusEqual(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return QOrder.order.status.eq(orderStatus);
    }

    /**
     * 전체 주문 조회 시, 회원(Member)과 배송(Delivery)도 한번에 가져오게끔 FETCH JOIN을 하는 메소드
     * 1. Lazy로 설정된 것도 무시하고 가져옴
     * 2. 프록시 객체가 아닌 실제 DB 정보를 가져옴
     * 페치 조인으로 Order -> Member , Order -> Delivery는 이미 조회된 상태이므로 지연로딩 발생 X
     */
    public List<Order> findAllWithMemberAndDelivery() {
        return entityManager.createQuery(
                "SELECT o FROM Order o " +
                        "JOIN FETCH o.member m " +
                        "JOIN FETCH o.delivery d", Order.class
        ).getResultList();
    }

    /**
     * distinct를 사용한 이유는 1대다 조인이 있으므로 데이터베이스 row가 증가한다.
     * 그 결과 같은 order 엔티티의 조회 수도 증가하게 된다.
     * JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가 조회되면,
     * 애플리케이션에서 중복을 걸러준다. 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.
     */
    public List<Order> findAllWithItem() {
        return entityManager.createQuery(
                        "SELECT DISTINCT o " +
                                "FROM Order o " +
                                "JOIN FETCH o.member m " +
                                "JOIN FETCH o.delivery d " +
                                "JOIN FETCH o.orderItems oi " +
                                "JOIN FETCH oi.item i", Order.class
                )
//                .setFirstResult(0)
//                .setMaxResults(100)
                .getResultList();
    }

    /**
     * V3.1 - 페이징 최적화를 위한 첫번째 메소드
     *
     * @param offset
     * @param limit
     * @return List<Order>
     */
    public List<Order> findAllWithMemberAndDelivery(int offset, int limit) {
        return entityManager.createQuery(
                        "SELECT o FROM Order o " +
                                "JOIN FETCH o.member m " +
                                "JOIN FETCH o.delivery d", Order.class
                )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
