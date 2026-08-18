package or.kr.bashboard.history.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.coin.repository.CoinRepository;
import or.kr.bashboard.coin.service.BinanceService;
import or.kr.bashboard.history.entity.PriceHistory;
import or.kr.bashboard.history.repository.PriceHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final CoinRepository coinRepository;
    private final BinanceService binanceService;

    // 10초마다 모든 코인 가격 저장
    @Scheduled(fixedDelay = 10000)
    public void savePriceHistory() {
        List<String> symbols = List.of("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT");

        symbols.forEach(symbol -> {
            binanceService.getLatestPrice(symbol).ifPresent(coinPrice -> {
                coinRepository.findBySymbol(symbol).ifPresent(coin -> {
                    priceHistoryRepository.save(
                            PriceHistory.builder()
                                    .coin(coin)
                                    .price(new BigDecimal(coinPrice.getPrice()))
                                    .volume(new BigDecimal(coinPrice.getVolume()))
                                    .recordedAt(LocalDateTime.now())
                                    .build()
                    );
                });
            });
        });
    }

    public Mono<List<Map<String, Object>>> getPriceHistory(String symbol, int page, int size) {
        return Mono.fromCallable(() ->
                priceHistoryRepository.findBySymbolOrderByRecordedAtDesc(
                                symbol,
                                PageRequest.of(page, size)
                        ).stream()
                        .map(h -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("price", h.getPrice().toPlainString());
                            map.put("volume", h.getVolume().toPlainString());
                            map.put("recordedAt", h.getRecordedAt().toString());
                            return map;
                        })
                        .toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
