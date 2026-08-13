package or.kr.bashboard.global.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.global.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = authentication.getAuthorities().toString();

        // JWT 발급
        String token = jwtProvider.generateToken(email, name, "google");

        log.info("로그인 성공: email={}, token 발급 완료", email);

        // 프론트로 토큰과 함께 리디렉션
        // 예: http://localhost:5173?token=eyJhbGc...
        String redirectUrl = "http://localhost:5173?token=" + token;

        webFilterExchange.getExchange().getResponse()
                .getHeaders()
                .setLocation(URI.create(redirectUrl));

        webFilterExchange.getExchange().getResponse()
                .setStatusCode(org.springframework.http.HttpStatus.FOUND);

        return webFilterExchange.getExchange().getResponse().setComplete();
    }
}
