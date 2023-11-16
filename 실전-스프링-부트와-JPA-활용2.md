## 실전-스프링-부트와-JPA-활용2



1. API 개발 기본

   - 회원등록  API

     - api 패키지를 따로 만드는 이유

       - api와 화면은 공통처리할 부분이 다르기 때문에 분리하는 것을 추천(사바사)

     - `@RestController`

       - `@Controller` + `@ResponseBody`

     - `@RequestBody`

       - HttpRequest의 본문 requestBody의 내용을 자바 객체로 매핑하는 역할
       - HTTP 통신의 body ==> 자바 객체

     - `@ResponseBody`

       - 자바 객체를 HttpResponse의 본문 responseBody의 내용으로 매핑하는 역할
       - 자바 객체 ==> HTTP 통신의 body

     - **Controller의 @RequestBody에 엔티티가 아닌 DTO를 사용하자**

       - 엔티티는 다양한 곳에서 사용할 수 있기 때문에 최대한 변하지 않는 것이 좋다
         - 예를 들어, 엔티티의 프로퍼티가 변하면 API 명세가 달라지기 때문에 문서화 + 타팀에 공지처럼 귀찮은 일이 발생할 수 있음
       - 화면에서 요구하는 데이터만 선택적으로 주는 것이 효율적
         - 예를 들어, 회원 가입은 간단한 이메일 가입부터 소셜링 간편 가입까지 다양하기 때문에 이 모든 요구사항을 하나의 엔티티로 충족시키는게 어려움
       - 엔티티는 외부에 노출하면 안 되기 때문에 API 명세서에서 쓰기 어려움

     - MemberApiController 코드

       - ```java
         @RestController
         @RequiredArgsConstructor
         public class MemberApiController {
             private final MemberService memberService;
         
             /**
              * Member 엔티티를 파라미터로 넘기는 API
              */
             @PostMapping("/api/v1/member")
             public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
                 Long id = memberService.join(member);
                 return new CreateMemberResponse(id);
             }
         
             /**
              * CreateMemberRequest DTO를 파마미터로 넘기는 API
              * API는 V1처럼 @RequestBody로 엔티티를 파라미터로 설정하면 큰일난다..
              * 엔티티는 다양한 곳에서 사용할 수 있기 때문에 최대한 변하지 않는 것이 좋음.
              * 화면에서 요구하는 데이터만 선택적으로 주는 것이 효율적.
              * 엔티티는 외부에 노출하면 안 되기 때문에 API 명세서에서 쓰기 어려움.
              */
             @PostMapping("/api/v2/member")
             public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
                 Member member = new Member();
                 member.setName(request.getName());
                 Long id = memberService.join(member);
                 return new CreateMemberResponse(id);
             }
         
             @Data
             static class CreateMemberRequest{
                 @NotBlank(message = "회원 이름은 공백일 수 없습니다.") // DTO에 validation 가능
                 private String name;
             }
         
             @Data
             static class CreateMemberResponse {
                 private Long id;
         
                 public CreateMemberResponse(Long id) {
                     this.id = id;
                 }
             }
         }
         ```

   - 회원 수정 API

     - MemberService

       - ```java
         @Transactional
         public void update(Long id, String name){
             Member member = memberRepository.findOne(id); //영속상태
             member.setName(name); //변경감지(Dirty Check) 발생하고, @Transactional에 의해서 트랜잭션 관련 AOP가 끝나면 JPA가 commit/flush 처리
         }
         ```

     - MemberApiController

       - ```java
         /**
         * 커맨드와 쿼리를 분리하는 습관을 가지는게 유지/보수에 좋다(CUD/R 분리)
         * 예를 들어 memberService.update()를 호출하고, Member 엔티티를 반환하면
         * 비영속 상태의 객체가 반환되기 때문에 유지/보수하기 어려울 수 있다.
         */
         @PutMapping("/api/v2/members/{id}")
         public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody UpdateMemberRequest request) {
             memberService.update(id, request.getName()); // 변경/추가 코드는 다른 조회성 코드와 혼재되지 않게 작성해주는 것을 추천
             Member foundMember = memberService.findOne(id);
             return new UpdateMemberResponse(foundMember.getId(), foundMember.getName());
         }
         ```

     - > 오류 정정
       >
       > 회원 수정 API updateMemberV2 은 회원 정보를 부분 업데이트 한다. 
       >
       > 여기서 PUT 방식을 사용했는데, PUT은 전체 업데이트를 할 때 사용하는 것이 맞다. 
       >
       > 부분 업데이트를 하려면 PATCH를 사용하거나 POST를 사용하는 것이 REST 스타일에 맞다.

   - 회원 조회 API

     - application.yml 수정

       - ```yaml
         spring:
           datasource:
             url: jdbc:h2:tcp://localhost/~/jpashop
             username: sa
             password:
             driver-class-name: org.h2.Driver
         
           jpa:
             hibernate:
               ddl-auto: none # create은 서버가 시작되면 모든 table을 삭제하고 다시 생성, none은 기존에 있었던 table 정보를 유지
             properties:
               hibernate:
                 show_sql: true
                 format_sql: true
         
         logging:
           level:
             org.hibernate.SQL: debug
             org.hibernate.type: trace
         ```

     - 회원 조회 V1

       - ```java
         @GetMapping("/api/v1/members")
         public List<Member> membersV1() {
             return memberService.findMembers();
         }
         ```

       - 문제점

         - ```
           엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
           
           기본적으로 엔티티의 모든 값이 노출된다.
            
           응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
           
           실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
           
           엔티티가 변경되면 API 스펙이 변한다.
           
           추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성으로
           해결)
           ```

       - `@JsonIgnore`

         - before
           - <img width="348" alt="image-20230911232536223" src="https://github.com/wooko5/JPA-Part2/assets/58154633/fc6fe99a-845b-4b4f-ac46-8b1ce23560a2">
         - after
           - ![image-20230911232750228](https://github.com/wooko5/JPA-Part2/assets/58154633/c778780f-0794-4867-82b2-412e712589ac)
         - `@JsonIgnore`는 엔티티에서 해당 칼럼을 JSON 데이터로 보여주지않기 위한 임시방편이지 절대 해결방안이 아님

       - 해결

         -  **API 응답 스펙에 맞추어 별도의 DTO를 반환한다.**

     - 회원 조회 V2

       - `Result` 클래스

         - JSON 리스트를 "data"라는 key로 포장하기 위한 클래스

         - ```java
           @Data
           @AllArgsConstructor
           static class Result<T> {
               private T data;
           }
           ```

       - JSON 리스트를 감싸는 이유

         - JSON 포맷의 확장성

         - ```
           /* 
           만약 count 같은 json 파라미터를 추가해달라고 하면, 
           적용 전의 json 리스트는 확장하기 어렵지만 
           적용 후의 json 리스트는 "data" 옆에 "count" : [] 형식으로 붙일 수 있다. 
           이렇게 API 스펙 확장에 열려있게 개발해야한다.
           */
           
           // before
           [
               {
                   "id": 1
               },
               {
                   "id": 2
               }
           ]
           
           // after
           {
               "data": [
                   {
                       "id": 1
                   },
                   {
                       "id": 2
                   }
               ]
           }
           ```
           
         - ![image-20230912002819985](https://github.com/wooko5/JPA-Part2/assets/58154633/66938185-d9bf-40ed-8da3-e37bc10b8e03)
         
           


2. API 개발 고급

   - API 개발 고급 소개

     - 실무에서는 저장/삭제/수정 작업보다는 조회가 훨씬 많이 발생하기 때문에 조회 관련된 문제에 대해서 최적화를 진행할 예정

   - 조회용 샘플 데이터 입력

     - initDb 코드

       - ```java
         package jpabook.jpashop;
         
         @Component
         @RequiredArgsConstructor
         public class initDb {
         
             private final InitService initService;
         
             @PostConstruct
             public void init() {
                 initService.dbInit1();
                 initService.dbInit2();
             }
         
         
             @Component
             @Transactional
             @RequiredArgsConstructor
             static class InitService {
         
                 private final EntityManager entityManager;
         
                 public void dbInit1() {
                     Member member = createInitialMember("Oh", "Seoul", "yeonhee-ro", "03171");
                     entityManager.persist(member);
         
                     Book book1 = createInitialBook("JPA Part1", 10000, 100);
                     entityManager.persist(book1);
         
                     Book book2 = createInitialBook("JPA Part2", 20000, 100);
                     entityManager.persist(book2);
         
                     OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
                     OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 1);
         
                     Delivery delivery = createInitialDelivery(member); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함
                     Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
                     entityManager.persist(order);
                 }
         
                 public void dbInit2() {
                     Member member = createInitialMember("Eom", "Busan", "oncheon-ro", "77717");
                     entityManager.persist(member);
         
                     Book book1 = createInitialBook("Spring Part1", 20000, 200);
                     entityManager.persist(book1);
         
                     Book book2 = createInitialBook("Spring Part2", 40000, 300);
                     entityManager.persist(book2);
         
                     OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
                     OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
         
                     Delivery delivery = createInitialDelivery(member); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함
                     Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
                     entityManager.persist(order);
                 }
         
                 private Member createInitialMember(String name, String city, String street, String zipcode) {
                     Member member = new Member();
                     member.setName(name);
                     member.setAddress(new Address(city, street, zipcode));
                     return member;
                 }
         
                 private Book createInitialBook(String name, int price, int stockQuantity) {
                     Book book1 = new Book();
                     book1.setName(name);
                     book1.setPrice(price);
                     book1.setStockQuantity(stockQuantity);
                     return book1;
                 }
         
                 private Delivery createInitialDelivery(Member member) {
                     Delivery delivery = new Delivery();
                     delivery.setAddress(member.getAddress()); // 실제로는 고객의 주소가 아닌 받는 사람의 배송지를 넣어야함 ==> 교육이니깐!!
                     return delivery;
                 }
             }
         }
         ```
       
     - TODO
     
       - ~~주문 내역에서 대표주문 1개만 나오는 문제를 모든 주문이 조회되게끔 바꿔보기(완료)~~
       
         
   
3. API 개발 고급 - 지연 로딩과 조회 성능 최적화

   - 개요

     - 주문 + 배송정보 + 회원을 조회하는 API를 만들자
     - 지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결해보자

   - 경고

     - > 참고: 지금부터 설명하는 내용은 정말 중요합니다. 실무에서 JPA를 사용하려면 100% 이해해야 합니다.
       >
       > 안그러면 엄청난 시간을 날리고 강사를 원망하면서 인생을 허비하게 됩니다. by young-han kim

   - 간단한 주문 조회 V1 : 엔티티를 직접 노출

     - jackson.databind.exc.InvalidDefinitionException

       - ByteBuddyInterceptor

       - ```java
         @Entity
         @Table(name = "orders")
         @Getter
         @Setter
         public class Order {
             @Id
             @GeneratedValue
             @Column(name = "order_id")
             private Long id;
         
             /*
             Order클래스의 Member는 Lazy전략 이기 때문에 포스트맨으로 Order 전체 조회를 하면, JPA에서 관련된 Member는 프록시 객체로 가짜로 넣어둔다.
             그래서 Order 전체 조회 시, Order와 관련된 Member를 조회할 때, 순수 객체가 아닌 프록시 객체인 ByteBuddyInterceptor()를 조회하려고 하니 '500' 에러 발생
             private Member member = new ByteBuddyInterceptor();
             */
             @ManyToOne(fetch = FetchType.LAZY)
             @JoinColumn(name = "member_id")
             private Member member;
         }
         ```

     - 해결 방법 - Hibernate5Module 을 스프링 빈으로 등록하면 해결

       - ```groovy
         /* build.gradle 설정 추가 스프링부트 3.0 이하 */
         implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
         
         /* 스프링부트 3.0 이상 */
         implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta'
         ```

       - 하지만 엔티티를 직접 노출하는 방법을 당연히 추천하지 않고, 교육이기 때문에 이런 것도 있다는 것만 알고 넘기자

       - ```java
         @SpringBootApplication
         public class JpashopApplication {
         
             public static void main(String[] args) {
                 SpringApplication.run(JpashopApplication.class, args);
             }
         
             @Bean //OrderSimpleApiController의 V1을 굳이 쓸려고 만든 코드, 실제로는 엔티티를 직접 노출해서 API를 설계하지 않는다
             Hibernate5Module hibernate5Module() {
                 Hibernate5Module hibernate5Module = new Hibernate5Module();
                 hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true); // 강제 지연 로딩 설정
                 return hibernate5Module;
             }
         
         }
         ```

     - 주의점

       > 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한 곳을 @JsonIgnore 처리 해야 한다. 안그러면 양쪽을 서로 호출하면서 무한 루프가 걸린다.
       >
       > 
       >
       > 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다! 즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다. 즉시 로딩으로 설정하면 성능튜닝이 매우 어려워 진다
       >
       > 
       >
       > DTO로 변환해서 반환하는 것이 더 좋은방법이다

     - 강조

       - **항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용하자! (V3에서 설명)**

   - 간단한 주문 조회 V2 : 엔티티를 DTO로 변환

     - 코드

       - ```java
         @GetMapping("/api/v2/simple-orders")
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
                 this.name = order.getMember().getName(); // Lazy 초기화
                 this.orderDate = order.getOrderDate();
                 this.orderStatus = order.getStatus();
                 this.address = order.getDelivery().getAddress(); // Lazy 초기화
             }
         }
         ```

     - V2 문제점 - `N + 1 문제`

       - Lazy loading으로인한 `1 + N`개의 쿼리가 콘솔창에 출력되는 문제가 발생 

       - ```
         - 쿼리가 총 1 + N + N 번 실행된다. (V1과 쿼리수 결과는 같다.)
         
         - order 조회 1번(order 조회 결과 수가 N이 된다. 여기서는 2개)
         order -> member 지연 로딩 조회 N 번 (2번)
         order -> delivery 지연 로딩 조회 N 번 (2번)
         총 1 + 2 + 2 = SQL문이 5번 콘솔창에 출력
         
         ex) 
         - order의 결과가 3개면 최악의 경우 1 + 3 + 3 번 실행된다.
         - order의 결과가 3개면 최선의 경우 1 + 1 + 1 번 실행된다.(모든 주문을 똑같은 사용자와 똑같은 배송지로 주문한 경우)
         
         
         - 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략
         ```

     - TIP

       - Address는 엔티티가 아니라 Value Object(값 타입)이라서 DTO로 정의하지 않아도 된다

   - 간단한 주문 조회 V3 : 엔티티를 DTO로 변환 - Fetch Join 최적화

     - 개념

       - ```
         - 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
         
         - 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태 이므로 지연로딩 X
         ```

     - 그 분의 한 마디

       - 실무에서 JPA를 적용해서 쓸려면 문제의 90% 이상은 `N+1`문제를 해결하지 못 하는 것
       - 고로 특별한 경우를 제외하고는 엔티티 간의 연관관계는 Lazy로 구현하고, Fetch Join을 이용해서 조회하면 대부분의 N+1 문제를 해결할 수 있다.
       - 아니면 GitHub처럼 아예 엔티티간의 모든 연관관계를 제거하고 논리적 외래키와 Fetch Join을 이용해서 성능을 끌어올릴 수도 있다.

     - TIP

       - Fetch Join에 대해 더 알고싶다면 JPA 기본 강좌와 책을 참고 

     - 문제점

       - Order 전체 조회 시, 사용하지 않는 Member와 Delivery의 프로퍼티도 가져오기 때문에 이를 없애야함 

   - 간단한 주문 조회 V4 : JPA에서 DTO로 바로 조회

     - Trade-off
     
       - V4는 DB에서 원하는 칼럼만 가져와서 DTO를 만듦
         - V3에 비해서 response data에 최적화된 DTO를 생성할 수 있음
         - 그러나 repository의 재사용성이 떨어짐,  API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점이기 때문에 화면에서 요구하는 데이터가 변경될때마다 repository에서 사용하는 쿼리문을 수정해야함
       - V3는 DB에서 모든 칼럼를 가져와서 controller/service에서 프론트에서 필요하는 DTO로 가공
     
     - SELECT절의 칼럼의 개수가 성능에 미비한 이유
     
       - 네트워크가 과거에 비해 훨씬 빨라짐
       - DB에서 TABLE의 JOIN을 어떤 방식으로 하냐에 따라서 성능에 끼치는 영향이 큼
     
     - 쿼리 방식 선택 권장 순서
     
       - ```tex
         1. 우선 엔티티를 DTO로 변환하는 방법(V2)을 선택한다.
         
         2. 필요하면 페치 조인으로 성능을 최적화(V3) 한다. 대부분의 성능 이슈가 해결된다.
         
         3. 그래도 안되면 DTO로 직접 조회하는 방법(V4)을 사용한다.
         
         4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
         ```
     
         

4. API 개발 고급 - 컬렉션 조회 최적화

   - 개요

     - 지금까지는 `XxxToOne`경우만 알아봤고, 이번에는 `XxxToMany`를 최적화하는 방법을 알아보자

   - 주문 조회 V1 : 엔티티 직접 노출

     - 기존의 간단한 주문 조회 V1과 동일

     - 코드

       - ```java
         @GetMapping("/api/v1/orders") // V1 : 엔티티 직접 노출 (절대 추천하지 않는 방법)
         public List<Order> ordersV1() {
             List<Order> allByString = orderRepository.findAllByString(new OrderSearch());
             for (Order order : allByString) {
                 order.getMember().getName(); // Lazy(지연로딩)에서 프록시를 강제 초기화
                 order.getDelivery().getAddress(); 
         
                 List<OrderItem> orderItems = order.getOrderItems();
                 orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy(지연로딩)에서 프록시를 강제 초기화
             }
             return allByString;
         }
         ```

   - 주문 조회 V2 : 엔티티를 DTO로 변환

     - InvalidDefinitionException - No serializer found for class
       - 문제발생 이유
         - 객체를 Json으로 serializer 하는 과정에서 접근 제한자가 **public** 이거나 **getter/setter**를 이용하기 때문에 인스턴스 필드를 private 등으로 선언하면 json으로 변환 과정에서 에러가 발생 ==> OrderDto, OrderItemDto 클래스의 getter/setter를 해주지않음
       - 해결방안
         - @Data 어노테이션 사용
         - @Data : @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 자동 적용
       
     - Collections.forEach VS stream.forEach
     
       - 단순 반복
     
         - 차이는 미비하지만 stream 객체를 생성하고 해제하는 stream().forEach보다 Collections.forEach가 좀더 효율적
     
       - 동시성 문제
     
         - Collections.forEach는 `synchronized` 키워드가 있어서 반복 중에 락이 걸려있기 때문에 멀티쓰레드 환경에서 안정적
     
           - 수정을 감지하면 즉시 ConcurrentModificationException을 던지고 프로그램 종료
           - ```java
             @Override
             public void forEach(Consumer<? super E> consumer) {
                 synchronized (mutex) {c.forEach(consumer);} // synchronized 키워드 때문에 lock 걸림
             }
             ```
         - stream().forEach는 반복 중에 다른 쓰레드에 의해 수정될 수 있고 무조건 끝까지 요소를 돌기때문에 일관성 없는 동작이 발생할 수 있다
     
           - 반복문이 모두 끝나고 나서 NullPointerException를 던지고 프로그램 종료
     
           - ```java
             @Override
             public Spliterator<E> spliterator() {
                 return c.spliterator(); // 사용자에 의해서 수동적으로 동기화를 해줘야함 ==> 락이 안 걸려있음
             }
             ```
     
       - 결론
     
         - ```
           1. Collections.forEach는 단순 반복할 때 stream().forEach보다 효율적
           
           2. Collections.stream().forEach은 원본 데이터를 수정하지 않고 가공하고 싶을 때 효율적
           
           3. Collections.parallelStream().forEach는 순서가 상관없는 병렬처리를 할 때 효율적
           ```
     
     - inner class에 static을 붙이는 이유
       - [outer class의 숨은 외부참조를 막아서 메모리 누수를 막을 수 있음](https://inpa.tistory.com/entry/JAVA-%E2%98%95-%EC%9E%90%EB%B0%94%EC%9D%98-%EB%82%B4%EB%B6%80-%ED%81%B4%EB%9E%98%EC%8A%A4%EB%8A%94-static-%EC%9C%BC%EB%A1%9C-%EC%84%A0%EC%96%B8%ED%95%98%EC%9E%90)
     
   - 주문 조회 V3 : 엔티티를 DTO로 변환 - Fetch Join 최적화

     - JPA에서 distinct
       - DB에 distict 키워드를 붙여줌
       - 같은 엔티티가 조회되면,애플리케이션에서 중복을 걸러준다. 예시로 order가 컬렉션(OneToMany) 페치 조인 때문에 중복 조회 되는 것을 막아준다.
       - distinct 적용 before
         - ![before](https://github.com/wooko5/JPA-Part2/assets/58154633/56440b49-85cc-43f5-8777-524053dc94cf)
         - orderItem이 총 4개이므로 order와 1대다 관계인 orderItem과 Fetch Join 적용하면 모든 데이터(4개)를 조회
       - distinct 적용 after
         - ![after](https://github.com/wooko5/JPA-Part2/assets/58154633/ae71a2eb-c8c6-4ce0-b4b9-bb6b771217db)
         - 중복없이 2개의 order만 조회 
       
     - 장점

       - Fetch Join으로 SQL이 1번만 실행됨
     
     - 단점
     
       - 페이징이 불가능
     
         - order 입장에서는 orderItem과는 OneToMany 관계이기 때문에 1대다 Fetch Join이 적용되면 데이터가 order 두 개가 아닌 orderItem을 기준으로 뻥튀기가 되어서 임의의 페이징이 불가능해짐
         - 예를 들어, 데이터가 1만 개 라면 전체 데이터를 어플리케이션단에 올리고 개발자가 아닌 메모리에서 페이징처리를 해버림 ==> out of memory 가능성 큼 ==> 끝장남
     
       - ```
         참고1
         - 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 
         - 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다.(매우 위험하다)
         
         참고2 
         - 컬렉션 페치 조인은 1개만 사용할 수 있다. 
         - 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가 부정합하게 조회될 수 있다.
         - 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.
         
         자세한 내용은 자바 ORM 표준 JPA 프로그래밍의 페치 조인 부분을 참고하자.
         ```
     
       - 메모리에서 페이징해버린 위험한 예시
     
         - ![image](https://github.com/wooko5/JPA-Part2/assets/58154633/1b1d57fb-0f23-4874-aa19-e613ec9fa0d3)
   
   
      - 주문 조회 V3.1 : 엔티티를 DTO로 변환 - 페이징과 한계 돌파
   
        - 컬렉션(1대N)을 페치 조인하면 페이징이 불가능
   
          - ```
            - 컬렉션을 페치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
            
            - 일다대에서 일(1)을 기준으로 페이징을 하는 것이 목적이다. 그런데 데이터는 다(N)를 기준으로 row가 생성된다.
            
            - Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하면 OrderItem이 기준이 되어버린다.
            
            (더 자세한 내용은 자바 ORM 표준 JPA 프로그래밍 - 페치 조인 한계 참조)
            ```
   
          - 이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징을 시도한다. 최악의 경우 Out of Memeory 장애로 이어질 수 있음
   
        - **해결방안**
   
          - ```
            1. XxxToOne(ex: OneToOne, ManyToOne) 관계는 모두 Fetch-Join한다.
            	- XxxToOne 관계는 row수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않는다.
            	- Order 입장에서 Member, Delivery는 확실한 XxxToOne 관계
            
            2. 컬렉션은 지연 로딩으로 조회한다
            
            3. 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용한다
            	- hibernate.default_batch_fetch_size: 글로벌 설정
            	- @BatchSize: 개별 최적화
            	- 해당 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다
            ```
            
          - V3는 쿼리 호출 수가 Order를 전체조회하는 조인 SQL 1개, OrderItem 2개만큼의 SQL, Item  4개만큼의 SQL이 로그창에 작성됨
   
            - 1 ==> 2(N) ==> 4(M)
   
          - V3.1은 쿼리 호출 수가  Order를 전체 조회하는 조인 SQL 1개, OrderItem을 조회하는 1개의 SQL, Item을 조회하는 1개의 SQL이 로그창에 작성됨
   
            - 1 ==> 1 ==> 1
            - 왜냐하면 limit 조건을 100으로 했기 때문에 100개 이하의 데이터는 한번에 IN 조건으로 출력한다. 만약 데이터가 1000개면 10번의 SQL을 출력
          
        - application.yml
   
          - ```yaml
            spring:
              datasource:
                url: jdbc:h2:tcp://localhost/~/jpashop
                username: sa
                password:
                driver-class-name: org.h2.Driver
            
              jpa:
                hibernate:
                  ddl-auto: create
                properties:
                  hibernate:
            #        show_sql: true
                    format_sql: true
                    default_batch_fetch_size: 100 # 페이징 사이즈설정
            
            logging:
              level:
                org.hibernate.SQL: debug
            #    org.hibernate.type: trace
            ```
        
        - > 참고
          >
          > default_batch_fetch_size 의 크기는 100~1000 사이를 선택하는 것을 권장한다. 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다. 
          >
          > 1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB에 순간부하가 증가할 수 있다. 
          >
          > 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다. 
          >
          > 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.
        
        - WAS든 DB든 가장 잘 버틸 수 있는 큰 수를 사이즈로 선택해야하는 것을 추천
   
          - 100개든 1000개든 전체 데이터를 로딩하기 때문에 WAS입장에서 메모리 사용량은 비슷함
   
   
      - 주문 조회 V4 : JPA에서 DTO 직접 조회
   
        - Order의  Repository 패키지를 나눈 이유
   
          - 비즈니스 로직과 관련된 엔티티를 찾을 때는 일단 Repository를 이용
          - 특정 화면에 적합한 쿼리를 생성할 때는 패키지를 나누어서 작성
   
        - OrderDto를 안 쓰고 OrderQueryDto를 만든 이유
   
          - OrderDto를 쓰면 Repository가 Controller를 참조한다. 즉 의존관계가 무한순환참조 문제가 발생할 수 있음
   
        - 정리
   
          - ```
            - Query: 맨 처음 Order JOIN Member/Delivery (XxxToOne 관계) 1번, 컬렉션(XxxToMany 관계)인 OrderItem 2(N)번 실행
            
            - XxxToOne(N:1, 1:1) 관계들을 먼저 조회하고, XxxToMany(1:N) 관계는 각각 별도로 처리한다.
            
            - 이런 방식을 선택한 이유는 다음과 같다.
            XxxToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
            XxxToMany(1:N) 관계는 조인하면 row 수가 증가한다.
            
            - row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고, ToMany 관계는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다
            ```
   
   
      - 주문 조회 V5 : JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
   
        - 코드
        
          - ```java
            public List<OrderQueryDto> findAllByDtoOptimization() {
                    //루트 조회(XxxToOne 코드를 모두 한번에 조회)
                    List<OrderQueryDto> result = findOrders();
            
                    // findOrderQueryDtos()처럼 loop를 돌지 않고, DB에서 OrderItem 데이터를 한번에 가져오는 코드를 작성
                    Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));
            
                    //loop를 돌면서 컬렉션 추가(추가 쿼리 실행X)
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
            ```
        
        - 장점
        
          - 데이터를 select하는 양이 줄어듦
        
        - 단점
        
          - Map 같은 메모리 상의 Collection에 데이터를 담아야하기 때문에 코드가 길어짐
   
      - 주문 조회 V6 : JPA에서 DTO 직접 조회 - 플랫 데이터 최적화
        - 장점
          - 1줄로 SQL문이 간결해진다
        - 단점
          - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
          - 애플리케이션에서 추가 작업이 크다.
          - 한번에 가져올 수 있지만 페이징 불가능
        
      - API 개발 고급 정리
   
        - 엔티티 조회
   
          - 엔티티를 조회해서 그대로 반환: V1
          - 엔티티 조회 후, DTO로 변환: V2
          - Fetch Join으로 쿼리 수 최적화: V3
          - 컬렉션 페이징과 한계 돌파: V3.1
            - 컬렉션은 Fetch Join 시, 페이징 처리 불가
            - XxxToOne 관계는 Fetch Join으로 쿼리 수 최적화'
            - 컬렉션은 Fetch Join 대신에 지연 로딩을 유지하고, `hibernate.default_batch_fetch_size`, `@BatchSize`로 최적화
   
        - DTO 직접 조회
   
          - JPA에서 DTO를 직접 조회: V4
          - 컬렉션 조회 최적화
            - 일대다 관계인 컬렉션은 `IN` 절을 활용해서 메모리에 미리 조회해서 최적화: V5
          - 플랫 데이터 최적화
            - JOIN 결과를 그대로 조회 후, 애플리케이션단에서 원하는 모양으로 직접 변환: V6 
   
        - 권장순서
   
          1. 엔티티 조회 방식으로 우선 접근 (엔티티 조회 후, Controller에서 DTO로 변환)
             - Fetch Join으로 쿼리 수 최적화
             - 컬렉션 최적화
               - 페이징 필요 O: `hibernate.default_batch_fetch_size`, `@BatchSize`로 최적화
               - 페이징 필요 X: Fetch Join 사용
          2. 엔티티 조회 방식으로 해결이 안 되면, DTO 직접조회 방식 사용
          3. DTO 직접 조회 방식으로 해결이 안 되면, `NativeSQL` or `Spring JdbcTemplate`을 사용
   
        - 참고- 개발자는 성능 최적화와 코든 복잡도 사이에서 밀당
   
          - ```
            엔티티 조회 방식은 Fetch Join이나, hibernate.default_batch_fetch_size , @BatchSize 같이 코드를 거의 수정하지 않고, 옵션만 약간 변경해서, 다양한 성능 최적화를 시도할 수 있다.
            
            반면에 DTO를 직접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.
            
            개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다. 항상 그런 것은 아니지만, 보통 성능 최적화는 단순한 코드를 복잡한 코드로 몰고간다.
            
            엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서, 성능을 최적화 할 수 있다.
            
            반면에 DTO 조회 방식은 SQL을 직접 다루는 것과 유사하기 때문에, 둘 사이에 줄타기를 해야 한다.
            ```
   
          - Fetch Join이나 hibernate.default_batch_fetch_size 등을 사용해서 최적화 하지 못 한 쿼리를 V4처럼 DTO 직접조회로 바꾼다 한들 큰 차이를 느끼기 쉽지않을 것. 차라리 캐시를 쓰는 등의 해결책이 나을 수 있음.
   
            - 엔티티를 직접 캐싱하면 안 됨 ==> DTO로 변환 후 DTO를 캐싱해야함
              - 엔티티는 영속성 컨텍스트에서 관리되기 때문에 캐시에 잘못 올라가면 서로 잘못된 정보를 보유할 수 있음
              - 예를 들어, remove()로 영속성 컨텍스트에서 해당 엔티티를 준영속 상태로 만들었는데 캐시에 있다면 제대로 지워지지 않음
   
        - DTO 조회 방식의 선택지
   
          - ```
            DTO로 조회하는 방법도 각각 장단이 있다. V4, V5, V6에서 단순하게 쿼리가 1번 실행된다고 V6이 항상좋은 방법인 것은 아니다.
            
            V4는 코드가 단순하다. 특정 주문 한건만 조회하면 이 방식을 사용해도 성능이 잘 나온다. 예를 들어서 조회한 Order 데이터가 1건이면 OrderItem을 찾기 위한 쿼리도 1번만 실행하면 된다.
            
            V5는 코드가 복잡하다. 여러 주문을 한꺼번에 조회하는 경우에는 V4 대신에 이것을 최적화한 V5 방식을 사용해야 한다. 예를 들어서 조회한 Order 데이터가 1000건인데, V4 방식을 그대로 사용하면, 쿼리가 총 1 + 1000번 실행된다. 
            
            여기서 1은 Order 를 조회한 쿼리고, 1000은 조회된 Order의 row 수다. V5 방식으로 최적화 하면 쿼리가 총 1 + 1번만 실행된다. 상황에 따라 다르겠지만 운영 환경에서 100배 이상의 성능 차이가 날 수 있다.
            
            V6는 완전히 다른 접근방식이다. 쿼리 한번으로 최적화 되어서 상당히 좋아보이지만, Order를 기준으로 페이징이 불가능하다. 실무에서는 이정도 데이터면 수백이나, 수천건 단위로 페이징 처리가 꼭 필요하므로, 이경우 선택하기 어려운 방법이다. 그리고 데이터가 많으면 중복 전송이 증가해서 V5와 비교해서 성능 차이도 미비하다.
            ```
   
          - V5가 V4보다 성능이 확실히 좋고, V6는 완전히 다른 접근법이라 비교하기 애매함(DTO 직접 조회 방식을 쓴다면 V5를 추천)
   
            
   
5. API 개발 고급 - 실무 필수 최적화

   - OSIV와 성능 최적화

     - 정의

       - Open Session In View == 하이버네이트
       - Open EntityManager In View == JPA

     - WARN 오류

       - ```java
         WARN 12468 --- [  restartedMain] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
         ```

       - 서버 실행 시, 시작 시점에 WARN 로그를 남김

     - spring.jpa.open-in-view : true (기본값)

       - 흐름도
         - ![image](https://github.com/wooko5/JPA-Part2/assets/58154633/8c8b69de-21b7-4423-ac5a-1cde60846e73)
       - 설명
         - OSIV 전략은 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점부터 API 응답이 끝날 때 까지 영속성 컨텍스트와 데이터베이스 커넥션을 유지한다. 
           - 지금까지 View Template이나 API 컨트롤러에서 지연 로딩이 가능
           - 영속성 컨텍스트와 DB와의 커넥션이 끝까지 살아있는 경우를 의미
           - HTML 렌더링이 모두 끝나고 클라이언트에게 모든 response가 도달하면 영속성 컨텍스트와 DB와의 커넥션이 끝남
       - 장점
         - 엔티티를 적극 활용해서 지연로딩을 컨트롤러나 뷰 같은 곳에서 적극활용할 수 있음
       - 단점
         - 오랜 시간 동안 데이터베이스 커넥션 리소스를 사용하기 때문에, 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있음 ==> 장애로 이어짐
         - 예를 들어, 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하고, 유지해야 한다.

     - spring.jpa.open-in-view : false (OSIV 종료)

       - 흐름도

         - ![image](https://github.com/wooko5/JPA-Part2/assets/58154633/7afdb447-4624-40b1-ad41-037162a9f027)

       - 설명

         - OSIV를 끄면, 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환 ==> 커넥션 리소스를 낭비하지 않음 (장점)
         - OSIV를 끄면 ,모든 지연로딩을 트랜잭션 안에서 처리해야 함 ==> 지금까지 작성한 많은 지연 로딩 코드를 트랜잭션 안으로 넣어야 하는 단점이 있음 (단점)
         - 그리고 view template에서 지연로딩이 동작하지 않음
         - 결론적으로 트랜잭션이 끝나기 전에 지연 로딩을 강제로 호출해 두어야 함

       - 코드

         - ```java
           /*
           Controller단
           */
           @PostMapping("/api/v1/member")
           public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
               Long id = memberService.join(member);
               return new CreateMemberResponse(id);
           }
           
           /*
           Service단
           */
           @Transactional
           public Long join(Member member) {
               validateDuplicateMember(member);
               memberRepository.save(member);
               return member.getId();
           }
           ```

         - OSIV를 끄면, Service단의 @Transactional을 벗어나는 순간 영속성 컨텍스트가 없어지면서 DB의 연결이 끊어짐

       - LazyInitializationException

         - ![image](https://github.com/wooko5/JPA-Part2/assets/58154633/db9c7b49-13f6-4bee-b724-7c6942f67ac4)
         - 예를 들어, Controller단의 memberService.join(member)으로 `Long id`가 반환되는 순간 영속성 컨텍스트가 없어지면서 DB의 연결이 끊어짐

     - 커멘드와 쿼리 분리

       - 실무에서 OSIV를 끈 상태로 복잡성을 관리하는 좋은 방법이 있다. 바로 Command와 Query를 분리하는 것
       - 예를 들어, OrderService를 두 개의 서비스로 분리하기
         - OrderService: 핵심 비즈니스 로직
         - OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 == 조회성 API)

     - TIP

       - 서비스의 실시간 API는 OSIV를 끄고
       - ADMIN 처럼 커넥션을 많이 사용하지 않는 곳에 서는 OSIV를 켬

6. 소개

   - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

     - 쓰는 이유

       - 미친 듯한 생산성
       - 공통적으로 많이 쓰는 SQL문이 이미 만들어져 있음

     - 구현체를 Spring bean이 injection 해야하지 않나?

       - 구현체를 Spring Data JPA의 API가 알아서 다 만들어서 주입함

     - TIP

       - ```
         스프링 데이터 JPA는 스프링과 JPA를 활용해서 애플리케이션을 만들때 정말 편리한 기능을 많이 제공한다. 단순히 편리함을 넘어서 때로는 마법을 부리는 것 같을 정도로 놀라운 개발 생산성의 세계로 우리를 이끌어 준다.
         
         하지만 스프링 데이터 JPA는 JPA를 사용해서 이런 기능을 제공할 뿐이다. 결국 JPA 자체를 잘 이해하는 것이 가장 중요하다
         ```

     - TODO

       - 원문 자료를 읽고,  JPA 책 완독하기

   - [QueryDsl](http://querydsl.com/)

     - 쓰는 이유

       - 직관적인 문법
       - 컴파일 시점에 빠른 문법 오류 발견
       - 코드 자동완성
       - 코드 재사용(결국 QueryDsl는 자바임)
       - JPQL new 명령어와는 비교가 안될 정도로 깔끔한 DTO 조회를 지원

     - build.gradle 코드

       - ```groovy
         //querydsl 추가
         buildscript {
             dependencies {
                 classpath("gradle.plugin.com.ewerk.gradle.plugins:querydsl-plugin:1.0.10")
             }
         }
         
         plugins {
             id 'java'
             id 'org.springframework.boot' version '2.7.13'
             id 'io.spring.dependency-management' version '1.0.15.RELEASE'
         }
         
         group = 'jpabook'
         version = '0.0.1-SNAPSHOT'
         sourceCompatibility = '11'
         
         //apply plugin: 'io.spring.dependency-management'
         apply plugin: "com.ewerk.gradle.plugins.querydsl"
         
         configurations {
             compileOnly {
                 extendsFrom annotationProcessor
             }
         }
         
         repositories {
             mavenCentral()
         }
         
         dependencies {
             implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
             implementation 'org.springframework.boot:spring-boot-starter-validation'
             implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
             implementation 'org.springframework.boot:spring-boot-starter-web'
             implementation 'org.springframework.boot:spring-boot-devtools'
             implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
         // implementation 'org.hibernate:hibernate-core:5.4.13.Final'
         
             implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6'
         
             compileOnly 'org.projectlombok:lombok'
             runtimeOnly 'com.h2database:h2'
         
             annotationProcessor 'org.projectlombok:lombok'
             testImplementation 'org.springframework.boot:spring-boot-starter-test'
             //추가
             testImplementation("org.junit.vintage:junit-vintage-engine") {
                 exclude group: "org.hamcrest", module: "hamcrest-core"
             }
         
             //querydsl 추가
             implementation 'com.querydsl:querydsl-jpa'
             //querydsl 추가
             implementation 'com.querydsl:querydsl-apt'
         }
         
         
         //querydsl 추가
         //def querydslDir = 'src/main/generated'
         def querydslDir = "$buildDir/generated/querydsl"
         
         querydsl {
             library = "com.querydsl:querydsl-apt"
             jpa = true
             querydslSourcesDir = querydslDir
         }
         
         sourceSets {
             main {
                 java {
                     srcDirs = ['src/main/java', querydslDir]
                 }
             }
         }
         
         compileQuerydsl{
             options.annotationProcessorPath = configurations.querydsl
         }
         
         configurations {
             querydsl.extendsFrom compileClasspath
         }
         ```

         
