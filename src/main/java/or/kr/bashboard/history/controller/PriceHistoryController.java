package or.kr.bashboard.history.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.history.service.PriceHistoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @QueryMapping
    public Mono<List<Map<String, Object>>> priceHistory(
            @Argument String symbol,
            @Argument Integer page,
            @Argument Integer size
    ) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 50;  // 기본 50개
        return priceHistoryService.getPriceHistory(symbol, p, s);
    }
}
