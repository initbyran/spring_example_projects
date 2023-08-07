package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.respository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
// JPA에서 중요한 것!!
// javax < springfamework : 쓸 수 있는 옵션들이 더 많다
// readOnly = true : 조회 성능 최적화 - 더티 체킹 안함, 읽기용 모드로 리소스 사용 적게
// -> 조회 아닌 경우 데이터 변경 안되므로 주의
@Transactional(readOnly = true)
//@AllArgsConstructor // 모든 필드로 생성자 만들어줌
@RequiredArgsConstructor // final이 있는 필드만 가지고 생성자를 만들어줌 (권장)
public class MemberService {

    // field injection : final 권장
    // -> compile 시점에 오류 체크 가능
    private final MemberRepository memberRepository;
    // setter injection : 비추천
    // contructor injection : 권장
    //    @Autowired : 생성자가 하나인 경우는 자동으로! (annotation 안붙여도됨)
    //    public MemberService(MemberRepository memberRepository){
    //        this.memberRepository = memberRepository;
    //    }

    /**
     * 회원가입
     */
    @Transactional // 우선 적용 : readOnly = false
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }
    //중복 회원 검증 : 동시에 가입하는 경우 문제가 될 수 있음 (실무에서는 다른 제약조건 필요; unique)
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers =
                memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
