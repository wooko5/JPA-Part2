package jpabook.jpashop;

import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    /**
     * 기본적으로 테스트가 끝나면 테스트에서 저장했던 데이터는 모두 롤백되어 삭제된다.
     * 그러나 @Rollback(false)을 선언하면 롤백이 되지않아서 데이터가 삭제되지 않는다.
     *
     * @Transactional이 없으면 테스트는 성공하지 못 한다.
     *
     * @throws Exception
     */
//    @Test
//    @Transactional
//    @Rollback(false)
//    public void testMember() throws Exception{
//        //Given
//        Member member = new Member();
//        member.setUsername("Jaeuk");
//
//        //When
//        Long savedId = memberRepository.save(member);
//        Member findedMember = memberRepository.find(savedId);
//
//        //Then
//        Assertions.assertThat(findedMember.getId()).isEqualTo(savedId);
//        Assertions.assertThat(findedMember.getUsername()).isEqualTo(member.getUsername());
//        /* 같은 영속성 컨텍스트 환경에서는 id값이 같으면 같은 객체로 취급하기에 true를 반환한다 */
//        Assertions.assertThat(findedMember).isEqualTo(member);
//    }

    @Test
    public void save() {
    }

    @Test
    public void find() {
    }
}