package tinyurl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    public void before() {
        urlRepository.deleteAll();
    }

    @Test
    public void testFindByShortUrl() {
        // given
        String shortUrl = "short_url";
        String longUrl = "long_url";
        urlRepository.save(new Url(shortUrl, longUrl));

        // when
        Optional<Url> invalidUrl = urlRepository.findByShortUrl("invalid");
        Optional<Url> url = urlRepository.findByShortUrl(shortUrl);

        // then
        Assertions.assertTrue(invalidUrl.isEmpty());
        Assertions.assertTrue(url.isPresent());
        Assertions.assertEquals(longUrl, url.get().getLongUrl());
    }

    @Test
    public void testDeleteByShortUrl() {
        // given
        String shortUrl = "short_url";
        String longUrl = "long_url";
        urlRepository.save(new Url(shortUrl, longUrl));

        // when
        urlRepository.deleteByShortUrl(shortUrl);
        Optional<Url> url = urlRepository.findByShortUrl(shortUrl);

        // then
        Assertions.assertTrue(url.isEmpty());
    }

    @Test
    public void testSaveExistingShortUrl() {
        // given
        String shortUrl = "short_url";
        String longUrl = "long_url";
        urlRepository.save(new Url(shortUrl, longUrl));

        // when
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> urlRepository.save(new Url(shortUrl, "new long_url")));

    }
}
