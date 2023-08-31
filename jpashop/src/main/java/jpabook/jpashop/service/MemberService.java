package jpabook.jpashop.service;

import jpabook.jpashop.api.MemberApiController;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
//@javax.transaction.Transactional /* 자바보다 스프링에서 제공하는 어노테이션을 쓰자(더 종류가 많다) */
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     * 전체 서비스 로직에 @Transactional(readOnly = true)을 줬는데,
     * 저장 로직인 경우 기본값인 @Transactional(readOnly = false)로 해줘야하기 때문에 따로 어노테이션을 선언함
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        /**
         * EXCEPTION 처리
         * 해당 로직의 경우 데이터의 안정성을 위해 DB의 Member 테이블의 name을 유니크 조건 걸어주는게 안전하다
         * 왜냐하면 findByName 같은 메소드를 호출할 때 실제로 Member의 데이터가 name을 갖고있어야 오류가 안 나기 때문
         */
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (findMembers.size() > 0) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }


    /* 회원 전체 조회 */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /* 회원 단건 조회 */
    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }

    @Transactional
    public void update(Long id, String name){
        Member member = memberRepository.findOne(id); //영속상태
        member.setName(name); //변경감지(Dirty Check) 발생하고, @Transactional에 의해서 트랜잭션 관련 AOP가 끝나면 JPA가 commit/flush 처리
    }
}
