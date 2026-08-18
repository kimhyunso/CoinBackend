package or.kr.bashboard.history.repository;

import or.kr.bashboard.history.entity.PriceHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // 특정 코인의 최근 히스토리 조회 (페이징)
    @Query("SELECT p FROM PriceHistory p WHERE p.coin.symbol = :symbol ORDER BY p.recordedAt DESC")
    List<PriceHistory> findBySymbolOrderByRecordedAtDesc(
            @Param("symbol") String symbol,
            Pageable pageable
    );
}
