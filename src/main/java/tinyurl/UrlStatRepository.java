package tinyurl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlStatRepository extends JpaRepository<UrlStat, Long> {
    @Query(value = "SELECT count(*) FROM url_stats WHERE url_id=? AND timestamp_millis>?", nativeQuery = true)
    long countBy(long urlId, long timestampMillis);
}
