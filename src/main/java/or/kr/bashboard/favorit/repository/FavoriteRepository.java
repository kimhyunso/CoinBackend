package or.kr.bashboard.favorit.repository;


import or.kr.bashboard.favorit.entity.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @EntityGraph(attributePaths = {"coin"})
    List<Favorite> findByMemberId(Long memberId);

    boolean existsByMemberIdAndCoinId(Long memberId, Long coinId);

    @Transactional
    void deleteByMemberIdAndCoinId(Long memberId, Long coinId);
}