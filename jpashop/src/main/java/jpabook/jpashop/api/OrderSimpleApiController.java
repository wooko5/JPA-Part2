package jpabook.jpashop.api;

import jpabook.jpashop.common.Result;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xxxToOne (ManyToOne, OneToOne)인 도메인만 한정(OneToMany인 OrderItem은 숙제)
 * Order 조회
 * Order ==> Member
 * Order ==> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders") // V1 : 엔티티를 직접 노출
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders") // V2 : 엔티티를 DTO로 변환
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        /**
         * Order 2개 ==> 여기서는 N은 2
         * 1 + N 문제 -> 1 + Member N개 + Delivery N개 == 총 5번 호출
         */
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());

        return new Result(result);
    }

    @GetMapping("/api/v3/simple-orders") // V3 : 엔티티를 DTO로 변환 - Fetch Join 최적화
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberAndDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());
        return new Result(result);
    }

    /**
     * V3에 비해서 response에 fit한 DTO를 생성할 수 있다
     * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트웍 용량 최적화(요즘에는 컴퓨터가 너무 좋아서 생각보다 미비하다)
     * V3에 비해서 repository 재사용성이 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
     */
    @GetMapping("/api/v4/simple-orders") // V4 : JPA(Repository)에서 DTO(OrderSimpleQueryDto)로 바로 조회
    public Result ordersV4() {
        return new Result(orderSimpleQueryRepository.findOrderDtos());
    }

    @Data
    private static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // 엔티티를 통해서 DTO를 생성하는 것은 괜찮다
        private SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // Lazy 초기화, SQL 출력
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // Lazy 초기화, SQL 출력
        }
    }
}
