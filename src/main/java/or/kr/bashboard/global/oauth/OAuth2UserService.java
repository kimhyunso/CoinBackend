package or.kr.bashboard.global.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.member.entity.Member;
import or.kr.bashboard.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return delegate.loadUser(userRequest)
                .flatMap(oAuth2User -> {
                    String provider = userRequest.getClientRegistration().getRegistrationId(); // google
                    String email = oAuth2User.getAttribute("email");
                    String name = oAuth2User.getAttribute("name");

                    log.info("OAuth2 로그인: provider={}, email={}", provider, email);

                    // DB에 없으면 자동 가입
                    return Mono.justOrEmpty(memberRepository.findByEmail(email))
                            .switchIfEmpty(
                                    Mono.fromCallable(() -> memberRepository.save(
                                            Member.builder()
                                                    .email(email)
                                                    .name(name)
                                                    .provider(provider)
                                                    .build()
                                    ))
                            )
                            .thenReturn(oAuth2User);
                });
    }
}
