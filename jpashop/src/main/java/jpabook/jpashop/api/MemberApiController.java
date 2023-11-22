package jpabook.jpashop.api;

import jpabook.jpashop.common.Result;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * 언뜻보면 굉장히 좋은 코드처럼 보이지만
     * List<Member>로 반환하면 사용자가 몰라도 되는 Member의 모든 정보를 노출시킴, API 명세를 만들기 힘듦
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collections = findMembers.stream()
                .map(member -> new MemberDto(member.getName(), member.getAddress()))
                .collect(Collectors.toList());

        return new Result(collections);
    }


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

    @Data //DTO를 inner class로 선언
    private static class CreateMemberRequest {
        @NotBlank(message = "회원 이름은 공백일 수 없습니다.(V2)") // DTO에 validation 가능
        private String name;
    }

    @Data
    @NoArgsConstructor
    private static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    private static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor //엔티티의 해당 어노테이션이나 @Setter 선언은 위험하지만 DTO는 상대적으로 자유롭다
    private static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class MemberDto {
        private String name;
        private Address address;
    }
}
