package or.kr.bashboard.coin.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinPrice {

    private String symbol;
    private String price;
    private String change;
    private String changePercent;
    private String volume;
    private String timestamp;
}
