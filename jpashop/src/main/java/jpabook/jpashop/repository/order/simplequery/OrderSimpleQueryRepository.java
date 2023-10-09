package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OrderSimpleQueryRepository {

    private final EntityManager entityManager;

    /**
     * 해당 메소드는 repository가 프론트(화면)단에 의지하고 있다.
     * 왜냐하면 화면에서 원하는 response data가 달라질 때마다 해당 쿼리를 수정해야하기 때문
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return entityManager.createQuery(
                "SELECT new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "FROM Order o " +
                        "JOIN o.member m " +
                        "JOIN o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }
}
