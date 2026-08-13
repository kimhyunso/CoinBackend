package or.kr.bashboard.favorit.service;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.coin.entity.Coin;
import or.kr.bashboard.coin.repository.CoinRepository;
import or.kr.bashboard.favorit.entity.Favorite;
import or.kr.bashboard.favorit.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CoinRepository coinRepository;

    // 즐겨찾기 목록 조회
    public Mono<List<Map<String, Object>>> getFavorites(Long memberId) {
        return Mono.fromCallable(() ->
                favoriteRepository.findByMemberId(memberId).stream()
                        .map(fav -> {
                            Coin coin = fav.getCoin();
                            Map<String, Object> coinMap = new HashMap<>();  // Map.of() 대신 HashMap 사용!
                            coinMap.put("id", coin.getId().toString());
                            coinMap.put("symbol", coin.getSymbol());
                            coinMap.put("name", coin.getName());
                            return coinMap;
                        })
                        .collect(Collectors.toList())
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // 즐겨찾기 추가
    public Mono<Coin> addFavorite(Long memberId, String symbol) {
        return Mono.fromCallable(() -> {
            Coin coin = coinRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new RuntimeException("코인을 찾을 수 없습니다: " + symbol));

            // 이미 즐겨찾기 된 경우 스킵
            boolean exists = favoriteRepository.existsByMemberIdAndCoinId(memberId, coin.getId());
            if (!exists) {
                favoriteRepository.save(
                        Favorite.builder()
                                .memberId(memberId)
                                .coin(coin)
                                .build()
                );
            }
            return coin;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 즐겨찾기 삭제
    public Mono<Boolean> removeFavorite(Long memberId, String symbol) {
        return Mono.fromCallable(() -> {
            Coin coin = coinRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new RuntimeException("코인을 찾을 수 없습니다: " + symbol));

            favoriteRepository.deleteByMemberIdAndCoinId(memberId, coin.getId());
            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}