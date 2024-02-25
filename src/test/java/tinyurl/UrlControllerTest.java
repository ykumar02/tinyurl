package tinyurl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tinyurl.random.RandomUrl;

import java.util.Map;
import java.util.Optional;

@WebMvcTest(UrlController.class)
@RunWith(SpringRunner.class)
public class UrlControllerTest {
    @MockBean
    private UrlRepository urlRepository;

    @MockBean
    private RandomUrl randomUrl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreate() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url(1, "shortUrl", "longUrl");

        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/url").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isCreated())
               .andDo(print());
    }

    @Test
    void testRedirect() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url(1, "shortUrl", "longUrl");

        when(urlRepository.findByShortUrl(any(String.class))).thenReturn(Optional.of(savedUrl));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isOk())
               .andExpect(content().string(savedUrl.getLongUrl()));
    }

    @Test
    void testDelete() throws Exception {
        // given
        Map<String, String> body = Map.of("longUrl", "http://www.example.com");
        Url savedUrl = new Url(1, "shortUrl", "longUrl");

        doNothing().when(urlRepository).deleteByShortUrl(any(String.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isNoContent());

        // test duplicate delete
        mockMvc.perform(MockMvcRequestBuilders.delete("/url/" + savedUrl.getShortUrl()))
               .andExpect(status().isNoContent());
    }
}