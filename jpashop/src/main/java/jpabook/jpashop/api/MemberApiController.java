package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
        @NotBlank(message = "회원 이름은 공백일 수 없습니다.(V2)") // DTO에 validation 가능
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
