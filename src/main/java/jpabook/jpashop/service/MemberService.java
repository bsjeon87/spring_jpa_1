package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //lazy loading이나 다른것들이 됨? (spring쪽에서 commit을해줌?) Transactional사용 추천)
                                // readOnly 옵션을 주면 jpa 최적화가 가능함.(default Transactional 옵션
@RequiredArgsConstructor //final 필드만을 포함해서 생성자를 만들줌. 아래 생성자 실제 코드가 필요없음.
                         // (생성자가하나인 경우 spring에서 autowired가 안붙어있어도 됨.)
public class MemberService {

    private final MemberRepository memberRepository;
/*
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
*/
    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); //동일한 이름을 허용하지 않는다는 비즈니스 로직을 가정.
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //was가 여러개 띄워서 운용되는 경우 최악의 경우 동시에 체크하여 넣어질수 있음.(ex:멀티스레드)
        // 그래서 db에 유니크 제약조건을 걸어놓는게 확실함.
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 이름의 회원입니다");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     *회원조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
