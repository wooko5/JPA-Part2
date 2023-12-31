package jpabook.jpashop.api;

import jpabook.jpashop.common.Result;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders") // V1 : 엔티티 직접 노출 (절대 추천하지 않는 방법)
    public List<Order> ordersV1() {
        List<Order> allByString = orderRepository.findAllByString(new OrderSearch());
        for (Order order : allByString) {
            order.getMember().getName(); // Lazy(지연로딩)에서 프록시를 강제 초기화
            order.getDelivery().getAddress(); // OSIV를 끄면, Service단의 @Transactional을 벗어나는 순간 영속성 컨텍스트가 없어지면서 DB의 연결이 끊어짐 ==> LazyInitializationException 발생

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(o -> o.getItem().getName()); // Collections.forEach()는 단순 반복할 때 stream().forEach()보다 효율적
        }
        return allByString;
    }

    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());
        return new Result(result);
    }

    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());
        return new Result(result);
    }

    /**
     * offset은 0부터 시작하는 INDEX를 의미
     * limit는 전체 데이터에서 잘라올 데이터 개수를 의미, EX) 데이터가 총 1000개면 페이징 10개가 생김
     * Order 입장에서 Member, Delivery는 확실한 XxxToOne 관계이기 때문에 한번에 Fetch-Join으로 처리해도 됨
     */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberAndDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());
        return new Result(result);
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDtoOptimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDtoFlat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Data
    private static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    private static class OrderItemDto {
        private Long id;
        private Item item;
        private Order order;
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            this.id = orderItem.getId();
            this.item = orderItem.getItem();
            this.order = orderItem.getOrder();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
