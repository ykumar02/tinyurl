package tinyurl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tinyurl.random.RandomUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

@WebMvcTest(UrlStatsController.class)
@RunWith(SpringRunner.class)
public class UrlStatsControllerTest {
    @MockBean
    private UrlRepository urlRepository;

    @MockBean
    private UrlStatRepository urlStatRepository;

    @MockBean
    private RandomUrl randomUrl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Clock clock;

    @Test
    void testUrlStats() throws Exception {
        // given
        Url savedUrl = new Url(1, "shortUrl", "longUrl");
        WindowStat dayStat = new WindowStat(WindowStat.Window.DAY, 100);
        WindowStat weekStat = new WindowStat(WindowStat.Window.WEEK, 1000);
        WindowStat allStat = new WindowStat(WindowStat.Window.ALL, 10000);
        Instant currentInstant = clock.instant();
        when(urlRepository.findByShortUrl(eq(savedUrl.getShortUrl()))).thenReturn(Optional.of(savedUrl));
        when(urlStatRepository.countBy(eq(savedUrl.getId()), eq(dayStat.window.getQueryTimestamp(currentInstant.toEpochMilli()))))
                .thenReturn(dayStat.count);
        when(urlStatRepository.countBy(eq(savedUrl.getId()), eq(weekStat.window.getQueryTimestamp(currentInstant.toEpochMilli()))))
                .thenReturn(weekStat.count);
        when(urlStatRepository.countBy(eq(savedUrl.getId()), eq(allStat.window.getQueryTimestamp(currentInstant.toEpochMilli()))))
                .thenReturn(allStat.count);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/urlStats/" + savedUrl.getShortUrl()))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(dayStat, weekStat, allStat))));
    }

    @Test
    void testUrlStatsUnavailable() throws Exception {
        // given
        Url savedUrl = new Url(1, "shortUrl", "longUrl");
        when(urlRepository.findByShortUrl(any(String.class))).thenThrow(new RuntimeException());

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/urlStats/" + savedUrl.getShortUrl()))
               .andExpect(status().isServiceUnavailable());

        // given
        when(urlRepository.findByShortUrl(any(String.class))).thenReturn(Optional.of(savedUrl));
        when(urlStatRepository.countBy(anyLong(), anyLong())).thenThrow(new RuntimeException());

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/urlStats/" + savedUrl.getShortUrl()))
               .andExpect(status().isServiceUnavailable());
    }

    @TestConfiguration
    public static class Config {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneOffset.UTC);
        }
    }
}
