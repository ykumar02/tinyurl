package tinyurl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    @Query(value = "SELECT * FROM urls WHERE short_url=?", nativeQuery = true)
    Optional<Url> findByShortUrl(String shortUrl);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM urls WHERE short_url=?", nativeQuery = true)
    void deleteByShortUrl(String shortUrl);
}
