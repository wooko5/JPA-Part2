## 실전-스프링-부트와-JPA-활용2



1. API 개발 기본

   - 회원등록  API

     - api 패키지를 따로 만드는 이유

       - api와 화면은 공통처리할 부분이 다르기 때문에 분리하는 것을 추천(사바사)

     - `@RestController`

       - `@Controller` + `@ResponseBody`

     - `@ResponseBody`

       - 자바 객체를 HttpResponse의 본문 responseBody의 내용으로 매핑하는 역할
       - 자바 객체 ==> HTTP 통신의 body

     - `@RequestBody`

       - HttpRequest의 본문 requestBody의 내용을 자바 객체로 매핑하는 역할
       - HTTP 통신의 body ==> 자바 객체

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

   - 회원 조회 API

2. API 개발 고급

3. API 개발 고급 - 지연 로딩과 조회 성능 최적화

4. API 개발 고급 - 컬렉션 조회 최적화

5. API 개발 고급 - 실무 필수 최적화

6. 정리
