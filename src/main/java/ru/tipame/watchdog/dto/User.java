package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.text.SimpleDateFormat;

/**
 * Created by tipame on 28.10.2017.
 */
public class User {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, MMM d");

    private int id;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean online;
    @JsonProperty("last_seen")
    private LastSeen lastSeen;
    @JsonProperty("online_mobile")
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean onlineMobile;
    @JsonProperty("screen_name")
    private String alias;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isOnline() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }

    public LastSeen getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LastSeen lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public void setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String toString() {
        String state = online ? "онлайн" : "был в сети "+dateFormat.format(lastSeen.getTime());
        return String.format("%s %s - %s (%s)",
                firstName,
                lastName,
                state,
                lastSeen.getPlatform().getName()
        );
    }
}
