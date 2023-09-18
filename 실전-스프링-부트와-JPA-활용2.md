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

     - > /오류정정/
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

         - ```json
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
         
                     //TODO: 주문을 2개 했는데 '주문내역' 화면에는 1개 주문만 출력 ==> 모든 주문이 조회된 화면으로 수정
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
     
       - 주문 내역에서 대표주문 1개만 나오는 문제를 모든 주문이 조회되게끔 바꿔보기
       
         
   
3. API 개발 고급 - 지연 로딩과 조회 성능 최적화

   - 개요

     - 주문 + 배송정보 + 회원을 조회하는 API를 만들자
     - 지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결해보자

   - 참고

     - > 참고: 지금부터 설명하는 내용은 정말 중요합니다. 실무에서 JPA를 사용하려면 100% 이해해야 합니다.
       >
       > 안그러면 엄청난 시간을 날리고 강사를 원망하면서 인생을 허비하게 됩니다.

   - 간단한 주문 조회 V1: 엔티티를 직접 노출

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
             @ManyToOne(fetch = FetchType.LAZY) // 모든 연관관계는 지연로딩으로 설정, 연관관계의 주인으로 본다
             @JoinColumn(name = "member_id") // 어떤 칼럼을 조인칼럼으로 쓸것인가? ==> Member의 member_id를 조인칼럼으로 쓰겠다(DB 관점)
             private Member member;
         }
         ```

         

   - 간단한 주문 조회 V2: 엔티티를 DTO로 변환

   - 간단한 주문 조회 V3: 엔티티를 DTO로 변환 - Fetch Join 최적화

   - 간단한 주문 조회 V4: JPA에서 DTO로 바로 조회

4. API 개발 고급 - 컬렉션 조회 최적화

5. API 개발 고급 - 실무 필수 최적화

6. 정리
