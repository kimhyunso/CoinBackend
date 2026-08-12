package or.kr.bashboard.coin.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.coin.model.CoinPrice;
import or.kr.bashboard.coin.service.BinanceService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class CoinController {

    private final BinanceService binanceService;

    // GraphQL Subscription
    // 클라이언트가 priceUpdated(symbol: "BTCUSDT") 구독하면
    // Binance에서 받은 실시간 가격을 계속 push
    @SubscriptionMapping
    public Flux<CoinPrice> priceUpdated(@Argument String symbol) {
        return binanceService.getPriceStream(symbol);
    }
}
