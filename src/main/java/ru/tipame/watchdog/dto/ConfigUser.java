package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by tipame on 28.10.2017.
 */
public class ConfigUser {

    private String id;
    private ConfigUser[] followers;
    @JsonProperty("show_anyway")
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean showAnyway;
    @JsonProperty("watch_online")
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean watchOnline;
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean disabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConfigUser[] getFollowers() {
        return followers;
    }

    public void setFollowers(ConfigUser[] followers) {
        this.followers = followers;
    }

    public boolean isShowAnyway() {
        return showAnyway;
    }

    public void setShowAnyway(boolean showAnyway) {
        this.showAnyway = showAnyway;
    }

    public boolean isWatchOnline() {
        return watchOnline;
    }

    public void setWatchOnline(boolean watchOnline) {
        this.watchOnline = watchOnline;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
