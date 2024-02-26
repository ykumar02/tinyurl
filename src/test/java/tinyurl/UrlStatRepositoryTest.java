package tinyurl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UrlStatRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UrlStatRepository urlStatRepository;

    @BeforeEach
    public void before() {
        urlRepository.deleteAll();
        urlStatRepository.deleteAll();
    }

    @Test
    public void testCountBy() {
        // given
        String shortUrl = "short_url";
        String longUrl = "long_url";
        Url url = urlRepository.save(new Url(shortUrl, longUrl));
        Url unknownUrl = new Url("unknown short_url", longUrl);
        Instant currentInstant = Instant.now();
        Instant oldInstant = currentInstant.minusMillis(ChronoUnit.DAYS.getDuration().toMillis());
        int statCount = 10;
        addUrlCount(urlStatRepository, url, statCount, currentInstant.toEpochMilli());
        addUrlCount(urlStatRepository, url, statCount, oldInstant.toEpochMilli());

        // when
        long currentCount = urlStatRepository.countBy(url.getId(), currentInstant.minusMillis(1).toEpochMilli());
        long oldCount = urlStatRepository.countBy(url.getId(), oldInstant.minusMillis(1).toEpochMilli());
        long totalCount = urlStatRepository.countBy(url.getId(), 0);
        long unknownCount = urlStatRepository.countBy(unknownUrl.getId(), oldInstant.minusMillis(1).toEpochMilli());

        // then
        Assertions.assertEquals(statCount, currentCount);
        Assertions.assertEquals(statCount * 2, oldCount);
        Assertions.assertEquals(statCount * 2, totalCount);
        Assertions.assertEquals(0, unknownCount);
    }

    private static void addUrlCount(UrlStatRepository urlStatRepository, Url url, int count, long millis) {
        for (int i = 0; i < count; i++) {
            urlStatRepository.save(new UrlStat(url, millis + i));
        }
    }
}
