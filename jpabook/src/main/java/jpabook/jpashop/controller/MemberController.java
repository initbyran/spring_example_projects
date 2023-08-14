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

    /**
     * 회원 등록 폼
     * @param model
     * @return
     */
    @GetMapping(value = "/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * 회원 등록 저장
     * @param form
     * @param result
     * @return
     */
    @PostMapping(value = "/members/new")
    // @Valid : @NotEmpty 외에 다양한 validation 기능을 어노테이션 기반으로 편리하게 사용가능하다
    // BindingResult : 오류 발생시 튕기지 않고 오류가 담겨서 코드가 실행됨(thymleaf-spring)
    public String create(@Valid MemberForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        memberService.join(member);
        return "redirect:/";
    }

    /**
     * 회원 목록 전체 조회
     * @param model
     * @return
     */
    // API 개발시 절대 entity를 외부로 반환해서는 안되는 이유
    // -> API 스펙이 변해버릴 수 있다(불완전성), 감춰야하는 정보가 밖으로 드러나게 될 수있다
    @GetMapping(value = "/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
