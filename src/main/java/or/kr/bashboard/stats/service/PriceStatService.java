package or.kr.bashboard.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.coin.repository.CoinRepository;
import or.kr.bashboard.stats.entity.PriceStat;
import or.kr.bashboard.stats.repository.PriceStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceStatService {

    private final PriceStatRepository priceStatRepository;
    private final CoinRepository coinRepository;

    // 가격 수신 시 고가/저가 업데이트
    @Transactional
    public void updateStat(String symbol, BigDecimal currentPrice) {
        LocalDateTime now = LocalDateTime.now();

        coinRepository.findBySymbol(symbol).ifPresent(coin -> {
            priceStatRepository.findByCoinSymbol(symbol)
                    .ifPresentOrElse(
                            // 기존 데이터 업데이트
                            stat -> stat.update(currentPrice, now),
                            // 처음 수신 시 생성
                            () -> priceStatRepository.save(
                                    PriceStat.builder()
                                            .coin(coin)
                                            .highPrice(currentPrice)
                                            .lowPrice(currentPrice)
                                            .highAt(now)
                                            .lowAt(now)
                                            .updatedAt(now)
                                            .build()
                            )
                    );
        });
    }

    // 특정 코인 고가/저가 조회
    public Mono<Map<String, Object>> getPriceStat(String symbol) {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            priceStatRepository.findByCoinSymbol(symbol).ifPresentOrElse(
                    stat -> {
                        result.put("highPrice", stat.getHighPrice().toPlainString());
                        result.put("lowPrice", stat.getLowPrice().toPlainString());
                        result.put("highAt", stat.getHighAt().toString());
                        result.put("lowAt", stat.getLowAt().toString());
                    },
                    () -> {
                        result.put("highPrice", "0");
                        result.put("lowPrice", "0");
                        result.put("highAt", "");
                        result.put("lowAt", "");
                    }
            );
            return result;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
