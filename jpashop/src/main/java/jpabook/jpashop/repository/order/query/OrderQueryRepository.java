package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager entityManager;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * Order기준으로 컬렉션(XxxToMany)인 OrderItem은 따로 조회
     * @param orderId
     * @return List<OrderItemQueryDto>
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return entityManager.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "FROM OrderItem oi " +
                        "JOIN oi.item i " +
                        "WHERE oi.order.id = :orderId ", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * Order와 Member, Delivery는 XxxToOne 관계이기 때문에 한번에 Join해서 조회
     */
    private List<OrderQueryDto> findOrders() {
        return entityManager.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "FROM Order o " +
                        "JOIN o.member m " +
                        "JOIN o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}
