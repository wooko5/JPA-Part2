package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        //@Valid를 통해 MemberForm에 이상한 값이 들어갔는지 검증 ==> name이 @NotEmpty이기 때문에 name이 공백인지 아닌지 검증
        if(result.hasErrors()){ //BindingResult 없을 때는 error 페이지가 나왔지만 BindingResult가 있다면 error를 핸들링할 수 있음 ==> createMemberForm.html을 유심히 보자
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        memberService.join(member);
        return "redirect:/"; //SpringMVC1에서 배운 PRG 패턴 적용으로 무분별한 중복 POST 요청 방지!!!
    }

    /**
     * 조회한 상품을 뷰에 전달하기 위해 스프링 MVC가 제공하는 모델(Model)객체에 보관
     * 실행할 뷰 이름을 반환
     */
    @GetMapping("/members")
    public String list(Model model){
        /**
         * 화면에서는 엔티티를 사용해도 괜찮지만, API에서는 절대 엔티티를 그대로 반환해서는 안 된다.
         * 왜냐하면 API는 명세이기 때문에 엔티티의 속성을 추가하면 문서에 또 작성해야 하고, 비밀번호 같은 속성이 추가되면 보안적인 이유도 존재한다.
         * 그래서 교육이니깐! 단순하게 엔티티인 Member를 반환했지만, DTO를 사용해서 정말 필요한 데이터만 있는 객체를 활용하자
         */
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
