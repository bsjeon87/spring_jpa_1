package jpabook.jpashop.service;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class) //spring이랑 엮어서 함께 테스트하겠다.
@SpringBootTest //spring boot를 띄운 상태로 테스트하겠다(없으면 autowired가 실패함.). 일반적으로 독립적으로 테스트하는게 더좋음( 더 빠르고 간결하므로)
@Transactional // spring의 transactional은  test에서 commit 하지 않고 rollback을 함.
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    //@Rollback(false) // test에서도 rollback하지 않도록함.
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        Assert.assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test
    public void 회원_중복_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim1");

        Member member2 = new Member();
        member2.setName("kim1");

        //when
        memberService.join(member1);
        try{
            memberService.join(member2); //예외 발생해야함. 발생하면 Exception으로 넘어가게됨.
        } catch (IllegalStateException e) {
            return;
        }

        //then
        Assert.fail("예외가 발생해야함.");
    }


}