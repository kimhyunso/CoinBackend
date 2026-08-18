package or.kr.bashboard.history.entity;

import jakarta.persistence.*;
import lombok.*;
import or.kr.bashboard.coin.entity.Coin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal volume;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
