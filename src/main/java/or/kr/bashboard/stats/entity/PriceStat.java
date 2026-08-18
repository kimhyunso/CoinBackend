package or.kr.bashboard.stats.entity;

import jakarta.persistence.*;
import lombok.*;
import or.kr.bashboard.coin.entity.Coin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_stat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PriceStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false, unique = true)
    private Coin coin;

    @Column(name = "high_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal lowPrice;

    @Column(name = "high_at", nullable = false)
    private LocalDateTime highAt;

    @Column(name = "low_at", nullable = false)
    private LocalDateTime lowAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 고가/저가 업데이트
    public void update(BigDecimal currentPrice, LocalDateTime now) {
        boolean updated = false;

        if (currentPrice.compareTo(this.highPrice) > 0) {
            this.highPrice = currentPrice;
            this.highAt = now;
            updated = true;
        }
        if (currentPrice.compareTo(this.lowPrice) < 0) {
            this.lowPrice = currentPrice;
            this.lowAt = now;
            updated = true;
        }
        if (updated) {
            this.updatedAt = now;
        }
    }
}
