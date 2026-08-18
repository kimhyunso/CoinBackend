package or.kr.bashboard.stats.controller;

import lombok.RequiredArgsConstructor;
import or.kr.bashboard.stats.service.PriceStatService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PriceStatController {

    private final PriceStatService priceStatService;

    @QueryMapping
    public Mono<Map<String, Object>> priceStat(@Argument String symbol) {
        return priceStatService.getPriceStat(symbol);
    }
}
