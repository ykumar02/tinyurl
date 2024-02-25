package tinyurl;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Immutable;
import org.springframework.lang.NonNull;


@Immutable
@Entity
@Table(name = "urls", uniqueConstraints = {@UniqueConstraint(columnNames = {"short_url"})})
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Nonnull
    @Column(name = "short_url")
    private String shortUrl;

    @NonNull
    @Column(name = "long_url")
    private String longUrl;

    public Url() {
    }

    public Url(long id, String shortUrl, String longUrl) {
        this.id = id;
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
    }

    public Url(String shortUrl, String longUrl) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String url) {
        this.longUrl = url;
    }

    @Override
    public String toString() {
        return "Url{id=" + id + ", shortUrl='" + shortUrl + "', longUrl='" + longUrl + "'}";
    }
}