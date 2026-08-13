package or.kr.bashboard.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.member.dto.SignupRequest;
import or.kr.bashboard.member.dto.LoginRequest;
import or.kr.bashboard.member.entity.Member;
import or.kr.bashboard.member.repository.MemberRepository;
import or.kr.bashboard.global.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    public void signup(SignupRequest request) {
        validatePassword(request.getPassword());
        Optional<Member> existing = memberRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            Member member = existing.get();
            // 소셜 로그인으로 가입된 이메일이면 가입 불가
            if (!member.getProvider().equals("local")) {
                throw new IllegalArgumentException(
                    member.getProvider() + " 소셜 로그인으로 가입된 이메일입니다."
                );
            }
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화 후 저장
        memberRepository.save(
            Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("local")
                .build()
        );

        log.info("일반 회원가입 완료: {}", request.getEmail());
    }

    // 로그인
    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 소셜 로그인 계정이면 일반 로그인 불가
        if (!member.getProvider().equals("local")) {
            throw new IllegalArgumentException(
                member.getProvider() + " 소셜 로그인으로 가입된 계정입니다."
            );
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 발급
        return jwtProvider.generateToken(member.getEmail(), member.getName(), "local");
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("비밀번호는 영문자를 포함해야 합니다.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("비밀번호는 숫자를 포함해야 합니다.");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException("비밀번호는 특수문자를 포함해야 합니다.");
        }
    }
}
