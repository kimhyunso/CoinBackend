package or.kr.bashboard.coin.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.coin.model.CoinPrice;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceService {

    private final ObjectMapper objectMapper;
    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

    // 심볼별 Sink 관리 (BTCUSDT → Sink, ETHUSDT → Sink ...)
    private final Map<String, Sinks.Many<CoinPrice>> sinkMap = new ConcurrentHashMap<>();

    // 앱 시작 시 기본 코인들 자동 구독
    @PostConstruct
    public void init() {
        connect("btcusdt");
        connect("ethusdt");
        connect("bnbusdt");
        connect("solusdt");
        connect("xrpusdt");
    }

    // 특정 심볼 구독 시작
    public void connect(String symbol) {
        String upperSymbol = symbol.toUpperCase();

        if (sinkMap.containsKey(upperSymbol)) {
            return;
        }

        Sinks.Many<CoinPrice> sink = Sinks.many().replay().limit(1);
        sinkMap.put(upperSymbol, sink);

        // Combined stream URL로 변경
        String url = "wss://data-stream.binance.vision/stream";

        // SUBSCRIBE 메시지
        String subscribeMsg = String.format(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"%s@ticker\"],\"id\":1}",
                symbol.toLowerCase()
        );

        client.execute(
                        URI.create(url),
                        session -> {
                            // 연결 후 SUBSCRIBE 메시지 전송
                            Mono<Void> subscribe = session.send(
                                    Mono.just(session.textMessage(subscribeMsg))
                            );

                            // 데이터 수신
                            Flux<Void> receive = session.receive()
                                    .map(WebSocketMessage::getPayloadAsText)
                                    .doOnNext(message -> {
                                        CoinPrice coinPrice = parse(message, upperSymbol);
                                        if (coinPrice != null) {
                                            log.info("가격 수신: {} = {}", upperSymbol, coinPrice.getPrice());
                                            sink.tryEmitNext(coinPrice);
                                        }
                                    })
                                    .doOnError(e -> log.error("Binance WebSocket 에러: {}", e.getMessage()))
                                    .doOnComplete(() -> log.info("Binance WebSocket 연결 종료: {}", upperSymbol))
                                    .then()
                                    .thenMany(Flux.never());

                            return subscribe.thenMany(receive).then();
                        }
                )
                .doOnError(e -> {
                    log.error("Binance 연결 실패 [{}]: {}", upperSymbol, e.getMessage());
                    sinkMap.remove(upperSymbol);
                })
                .subscribe();

        log.info("Binance WebSocket 연결 시작: {}", upperSymbol);
    }

    // 특정 심볼의 실시간 가격 Flux 반환
    // GraphQL Subscription에서 이 Flux를 구독함
    public Flux<CoinPrice> getPriceStream(String symbol) {
        String upperSymbol = symbol.toUpperCase();

        // 구독 요청이 오면 없는 심볼은 자동 연결
        if (!sinkMap.containsKey(upperSymbol)) {
            connect(symbol);
        }

        return sinkMap.get(upperSymbol).asFlux();
    }

    // Binance JSON 파싱
    // Binance ticker 응답 형식:
    // { "s": "BTCUSDT", "c": "67842.50", "p": "+1234.20", "P": "+1.85", "v": "12453.231", "T": 1714912345678 }
    private CoinPrice parse(String message, String symbol) {
        try {
            JsonNode node = objectMapper.readTree(message);

            // Combined stream은 {"stream":"...","data":{...}} 형식
            // data 필드가 있으면 꺼내서 사용
            JsonNode data = node.has("data") ? node.get("data") : node;

            if (!data.has("c") || !data.has("p") || !data.has("P") || !data.has("v") || !data.has("T")) {
                log.debug("ticker 데이터 아님, 스킵: {}", message);
                return null;
            }

            return CoinPrice.builder()
                    .symbol(symbol)
                    .price(data.get("c").asText())
                    .change(data.get("p").asText())
                    .changePercent(data.get("P").asText())
                    .volume(data.get("v").asText())
                    .timestamp(data.get("T").asText())
                    .build();

        } catch (Exception e) {
            log.error("Binance 메시지 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
