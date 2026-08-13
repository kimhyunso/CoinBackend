package or.kr.bashboard.favorit.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.favorit.service.FavoriteService;
import or.kr.bashboard.member.entity.Member;
import or.kr.bashboard.member.repository.MemberRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final MemberRepository memberRepository;

    // 즐겨찾기 목록 조회
    @QueryMapping
    public Mono<List<Map<String, Object>>> favorites() {
        return getCurrentMember()
                .flatMap(member -> favoriteService.getFavorites(member.getId()))
                .defaultIfEmpty(List.of());
    }

    // 즐겨찾기 추가
    @MutationMapping
    public Mono<Map<String, Object>> addFavorite(@Argument String symbol) {
        return getCurrentMember()
                .flatMap(member -> favoriteService.addFavorite(member.getId(), symbol))
                .map(coin -> Map.of(
                        "id", coin.getId().toString(),
                        "symbol", coin.getSymbol(),
                        "name", coin.getName()
                ));
    }

    // 즐겨찾기 삭제
    @MutationMapping
    public Mono<Boolean> removeFavorite(@Argument String symbol) {
        return getCurrentMember()
                .flatMap(member -> favoriteService.removeFavorite(member.getId(), symbol));
    }

    // 현재 로그인 유저 조회
    private Mono<Member> getCurrentMember() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(Authentication::getName)
                .flatMap(email -> Mono.fromCallable(
                        () -> memberRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                ).subscribeOn(Schedulers.boundedElastic()));
    }
}