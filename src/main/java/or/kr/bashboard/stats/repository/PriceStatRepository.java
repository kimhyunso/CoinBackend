package or.kr.bashboard.stats.repository;

import or.kr.bashboard.stats.entity.PriceStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceStatRepository extends JpaRepository<PriceStat, Long> {

    @Query("SELECT ps FROM PriceStat ps WHERE ps.coin.symbol = :symbol")
    Optional<PriceStat> findByCoinSymbol(@Param("symbol") String symbol);
}
