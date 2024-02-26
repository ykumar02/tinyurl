package tinyurl;

import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tinyurl.random.RandomUrl;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
public class UrlController {
    static final int MAX_ATTEMPTS = 5;
    static final String URL_PREFIX = "http://tinyurl.com/";
    private static final Logger log = LogManager.getLogger();
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private UrlStatRepository urlStatRepository;
    @Autowired
    private RandomUrl randomUrl;
    @Autowired
    private Clock clock;


    @GetMapping("/url/{shortUrl}")
    public String redirect(@PathVariable String shortUrl) {
        Optional<Url> optUrl;
        try {
            optUrl = urlRepository.findByShortUrl(shortUrl);
        } catch (Exception ex) {
            log.error("Failed fetching url for short url.", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to find long url, please try again later.");
        }

        CompletableFuture.runAsync(() -> {
            try {
                optUrl.ifPresent(url -> urlStatRepository.save(new UrlStat(url, clock.millis())));
            } catch (Exception ex) {
                log.error("Unable to save url stats.", ex);
            }
        });

        return optUrl.map(Url::getLongUrl)
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No url found for the short url provided."));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/url/{shortUrl}")
    public void delete(@PathVariable String shortUrl) {
        try {
            urlRepository.deleteByShortUrl(shortUrl);
        } catch (Exception ex) {
            log.error("Failed deleting the short url.", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to delete short url, please try again later.");
        }
    }

    @PostMapping("/url")
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody Map<String, String> body) {
        String longUrl = body.get("longUrl");
        if (StringUtils.isEmpty(longUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide a long url to be shortened.");
        }
        String shortUrl = randomUrl.generate();
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            try {
                return URL_PREFIX + urlRepository.save(new Url(shortUrl, longUrl)).getShortUrl();
            } catch (DataIntegrityViolationException ex) {
                attempt++;
                shortUrl = randomUrl.generate();
            } catch (Exception ex) {
                log.error("Failed creating short url.", ex);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to create short url, please try again later.");
            }
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to create short url, please retry.");
    }
}
