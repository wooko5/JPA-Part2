package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     *
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

//    public List<OrderQueryDto> findAllByDtoOptimization() {
//        List<OrderQueryDto> result = findOrders();
//        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));
//        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
//        return result;
//    }
//
//    private List<Long> toOrderIds(List<OrderQueryDto> result) {
//        return result.stream()
//                .map(o -> o.getOrderId())
//                .collect(Collectors.toList());
//    }
//
//    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> ordersIds) {
//        List<OrderItemQueryDto> orderItems = entityManager.createQuery(
//                        "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, oi.item.name, oi.orderPrice, oi.count) " +
//                                "FROM OrderItem oi " +
//                                "JOIN oi.item i " +
//                                "WHERE oi.order.id in :ordersIds", OrderItemQueryDto.class)
//                .setParameter("ordersIds", ordersIds)
//                .getResultList();
//
//        return orderItems.stream()
//                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId)); //OrderItemQueryDto의 데이터를 orderId를 기준으로 그룹화
//    }
}
