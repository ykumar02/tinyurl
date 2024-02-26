package tinyurl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class UrlStatsController {
    private static final Logger log = LogManager.getLogger();
    private static final String ERROR_MSG = "Unable to get url stats, please try again later.";
    private final ExecutorService statsExecutorService = Executors.newFixedThreadPool(10);

    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private UrlStatRepository urlStatRepository;
    @Autowired
    private Clock clock;

    @GetMapping("/urlStats/{shortUrl}")
    @ResponseBody
    public List<WindowStat> urlStats(@PathVariable String shortUrl) {
        long currentMillis = clock.millis();
        Optional<Url> optUrl;
        try {
            optUrl = urlRepository.findByShortUrl(shortUrl);
        } catch (Exception ex) {
            log.error("Failed fetching url by short url.", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ERROR_MSG);
        }

        return optUrl.map(url -> {
            try {
                return statsExecutorService.submit(() -> Arrays
                        .stream(WindowStat.Window.values())
                        .parallel()
                        .map(window -> new WindowStat(window, urlStatRepository.countBy(url.getId(),
                                                                                        window.getQueryTimestamp(currentMillis))))
                        .collect(Collectors.toList())).get();
            } catch (Exception ex) {
                log.error("Failed fetching stats for url.", ex);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ERROR_MSG);
            }
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No stats available for the short url"));
    }
}
