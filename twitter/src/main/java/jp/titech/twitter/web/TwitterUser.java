package jp.titech.twitter.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

public class TwitterUser {
    private long id;

    @Length(max = 3)
    private String screenName;

    public TwitterUser() {
        // Jackson deserialization
    }

    public TwitterUser(long id, String screenName) {
        this.id = id;
        this.screenName = screenName;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String getScreenName() {
        return screenName;
    }
}
