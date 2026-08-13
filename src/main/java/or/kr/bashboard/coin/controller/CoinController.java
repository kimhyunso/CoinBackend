package or.kr.bashboard.coin.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.coin.modal.CoinPrice;
import or.kr.bashboard.coin.service.BinanceService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CoinController {

    private final BinanceService binanceService;

    // 실시간 가격 구독
    @SubscriptionMapping
    public Flux<CoinPrice> priceUpdated(@Argument String symbol) {
        return binanceService.getPriceStream(symbol);
    }

    // 전체 코인 목록
    @QueryMapping
    public Mono<List<Map<String, Object>>> coins() {
        return Mono.just(List.of(
                Map.of("id", "1", "symbol", "BTCUSDT", "name", "Bitcoin"),
                Map.of("id", "2", "symbol", "ETHUSDT", "name", "Ethereum"),
                Map.of("id", "3", "symbol", "BNBUSDT", "name", "BNB"),
                Map.of("id", "4", "symbol", "SOLUSDT", "name", "Solana"),
                Map.of("id", "5", "symbol", "XRPUSDT", "name", "XRP")
        ));
    }
}