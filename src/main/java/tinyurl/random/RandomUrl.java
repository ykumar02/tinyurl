package tinyurl.random;

import org.springframework.stereotype.Service;

@Service
public interface RandomUrl {
    String generate();
}
