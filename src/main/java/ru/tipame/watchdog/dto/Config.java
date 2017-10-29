package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by tipame on 28.10.2017.
 */
public class Config {

    private int delay;
    private int active;
    @JsonProperty("vk_token")
    private String vkToken;
    @JsonProperty("telegram_token")
    private String telegramToken;
    @JsonProperty("telegram_chat")
    private String telegramChat;
    @JsonProperty("send_telegram")
    @JsonDeserialize(using=NumericBooleanDeserializer.class)
    private boolean sendTelegram;

    private ConfigUser[] users;

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public ConfigUser[] getUsers() {
        return users;
    }

    public void setUsers(ConfigUser[] users) {
        this.users = users;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getVkToken() {
        return vkToken;
    }

    public void setVkToken(String vkToken) {
        this.vkToken = vkToken;
    }

    public String getTelegramToken() {
        return telegramToken;
    }

    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public String getTelegramChat() {
        return telegramChat;
    }

    public void setTelegramChat(String telegramChat) {
        this.telegramChat = telegramChat;
    }

    public boolean isSendTelegram() {
        return sendTelegram;
    }

    public void setSendTelegram(boolean sendTelegram) {
        this.sendTelegram = sendTelegram;
    }
}
