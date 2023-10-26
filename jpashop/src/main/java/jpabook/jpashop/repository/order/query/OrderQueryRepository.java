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

    public List<OrderQueryDto> findAllByDtoOptimization() {
        // 루트 조회(XxxToOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        // findOrderQueryDtos()처럼 loop를 돌지 않고, DB에서 OrderItem 데이터를 한번에 가져오는 코드를 작성
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // loop를 돌면서 컬렉션 추가(추가 쿼리 실행X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = entityManager.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "FROM OrderItem oi " +
                        "JOIN oi.item i " +
                        "WHERE oi.order.id IN :orderIds ", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // DB에서 가져온 OrderItem 데이터를 Map에 세팅
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    // OrderQueryDto의 orderId를 모두 뽑아내서 List<Long>를 반환
    private List<Long> toOrderIds(List<OrderQueryDto> orderQueryDtos) {
        return orderQueryDtos.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    public List<OrderFlatDto> findAllByDtoFlat() {
        return entityManager.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) " +
                        "FROM Order o " +
                        "JOIN o.member m " +
                        "JOIN o.delivery d " +
                        "JOIN o.orderItems oi " +
                        "JOIN oi.item i", OrderFlatDto.class
        ).getResultList();
    }
}
