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

@Controller
@RequiredArgsConstructor
public class MemberController {

    private  final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        //@Valid flag를 보고 spring에서 validation(notempty name)을 진행함.
        //BindingResult가 @Valid flag뒤에 있으면 valid결과를 넣어줌. 만약 없는상태에서 validate가 실패하는 경우 함수 코드가 수행되지 않고 나감.(error)

        if (result.hasErrors()) {
            // 화면 전환 시 argument도 그대로 전달(forwarding)해줌. MemberForm 과 /  BindingResult의 결과를 넘겨줌.
            // tymleaf에서 #fields.hasErrors 를 가지고 렌더링할때 추가 정보를 알려줌.
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //home으로
    }
}
