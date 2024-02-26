package tinyurl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tinyurl.UrlController.MAX_ATTEMPTS;
import static tinyurl.UrlController.URL_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tinyurl.random.RandomUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@WebMvcTest(UrlController.class)
@RunWith(SpringRunner.class)
public class UrlControllerTest {
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
    void testCreate() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        String shortUrl = "short";
        Url savedUrl = new Url(shortUrl, "longUrl");

        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);
        when(randomUrl.generate()).thenReturn(shortUrl);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isCreated())
               .andExpect(content().string(URL_PREFIX + shortUrl));
    }

    @Test
    void testRedirect() throws Exception {
        // given
        Url savedUrl = new Url("shortUrl", "longUrl");
        UrlStat urlStat = new UrlStat(savedUrl, clock.millis());

        when(urlRepository.findByShortUrl(any(String.class))).thenReturn(Optional.of(savedUrl));
        when(urlStatRepository.save(eq(urlStat))).thenReturn(urlStat);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isOk())
               .andExpect(content().string(savedUrl.getLongUrl()));
        verify(urlStatRepository).save(any());
        verifyNoMoreInteractions(urlStatRepository);
    }

    @Test
    void testDelete() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url("shortUrl", "longUrl");

        doNothing().when(urlRepository).deleteByShortUrl(any(String.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isNoContent());

        // test duplicate delete
        mockMvc.perform(MockMvcRequestBuilders.delete("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isNoContent());
    }

    @Test
    void testRedirectUnavailable() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url("shortUrl", "longUrl");
        UrlStat urlStat = new UrlStat(savedUrl, clock.millis());

        when(urlRepository.findByShortUrl(any(String.class))).thenThrow(new RuntimeException());
        when(urlStatRepository.save(any())).thenReturn(urlStat);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isServiceUnavailable());
        verifyNoInteractions(urlStatRepository);
    }

    @Test
    void testDeleteUnavailable() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url(1, "shortUrl", "longUrl");

        doThrow(new RuntimeException()).when(urlRepository).deleteByShortUrl(any(String.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isServiceUnavailable());
    }

    @Test
    void testCreateMultipleAttempts() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        String existingShortUrl = "existing";
        String newShortUrl = "new";
        Url newUrl = new Url(newShortUrl, "longUrl");

        when(urlRepository.save(any())).thenThrow(new DataIntegrityViolationException("test")).thenReturn(newUrl);
        when(randomUrl.generate()).thenReturn(existingShortUrl, newShortUrl);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isCreated())
               .andDo(print());
        verify(randomUrl, times(2)).generate();
        verify(urlRepository, times(2)).save(any());
        verifyNoMoreInteractions(randomUrl);
        verifyNoMoreInteractions(urlRepository);
    }

    @Test
    void testCreateUnavailable() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");

        when(urlRepository.save(any(Url.class))).thenThrow(new RuntimeException());

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isServiceUnavailable())
               .andDo(print());
    }

    @Test
    void testCreateBadRequest() throws Exception {
        // given
        Map<String, String> body = Map.of();

        when(urlRepository.save(any(Url.class))).thenThrow(new RuntimeException());

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateConflict() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");

        when(urlRepository.save(any(Url.class))).thenThrow(new DataIntegrityViolationException("test"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isConflict());
        verify(urlRepository, times(MAX_ATTEMPTS)).save(any());
    }

    @TestConfiguration
    public static class Config {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneOffset.UTC);
        }
    }
}
