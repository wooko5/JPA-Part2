package jpabook.jpashop.repository;

import jpabook.jpashop.common.Result;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OrderRepository {

    private final EntityManager entityManager;

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

    public Result findOrderDtos() {
        return new Result(
                entityManager.createQuery(
                                "SELECT new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                        "FROM Order o " +
                                        "JOIN FETCH o.member m " +
                                        "JOIN FETCH o.delivery d", OrderSimpleQueryDto.class)
                        .getResultList()
        );
    }
}
