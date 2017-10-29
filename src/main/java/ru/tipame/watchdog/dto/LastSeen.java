package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

/**
 * Created by tipame on 28.10.2017.
 */
public class LastSeen {

    @JsonDeserialize(using=UnixTimeDeserializer.class)
    private Date time;
    @JsonDeserialize(using=PlatformDeserializer.class)
    private Platform platform;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
