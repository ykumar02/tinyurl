package tinyurl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {
    private static final Logger log = LogManager.getLogger();

    @GetMapping("/greeting/{name}")
    public String get(@PathVariable String name) {
        return "hello, " + name;
    }
}
