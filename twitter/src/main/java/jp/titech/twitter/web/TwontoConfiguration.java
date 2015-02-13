package jp.titech.twitter.web;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TwontoConfiguration extends Configuration {

    @NotEmpty
    private String defaultName = "BarackObama";
    
    @NotEmpty
    private String defaultKeyword = "programming";

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
    
    @JsonProperty
    public String getDefaultKeyword() {
        return defaultKeyword;
    }

    @JsonProperty
    public void setDefaultKeyword(String keyword) {
        this.defaultKeyword = keyword;
    }
}