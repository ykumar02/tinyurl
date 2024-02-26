package tinyurl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
final class WindowStat {
    final Window window;
    final long count;

    WindowStat(Window window, long count) {
        this.window = window;
        this.count = count;
    }

    enum Window {
        DAY(ChronoUnit.DAYS.getDuration().toMillis()),
        WEEK(ChronoUnit.WEEKS.getDuration().toMillis()),
        ALL;

        final Optional<Long> duration;

        Window() {
            duration = Optional.empty();
        }

        Window(long duration) {
            this.duration = Optional.of(duration);
        }

        long getQueryTimestamp(long millis) {
            return duration.map(d -> millis - d).orElse(0L);
        }
    }
}
