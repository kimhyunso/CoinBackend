package or.kr.bashboard.member.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.member.dto.LoginRequest;
import or.kr.bashboard.member.dto.SignupRequest;
import or.kr.bashboard.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    // POST /api/auth/signup
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, String>>> signup(@RequestBody SignupRequest request) {
        return Mono.fromCallable(() -> {
            memberService.signup(request);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(IllegalArgumentException.class, e ->
            Mono.just(ResponseEntity.badRequest().body(Map.of("message", e.getMessage())))
        );
    }

    // 로그인
    // POST /api/auth/login
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> {
            String token = memberService.login(request);
            return ResponseEntity.ok(Map.of("token", token));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(IllegalArgumentException.class, e ->
            Mono.just(ResponseEntity.badRequest().body(Map.of("message", e.getMessage())))
        );
    }
}
