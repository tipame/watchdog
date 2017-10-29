package ru.tipame.watchdog.dto;

/**
 * Created by tipame on 28.10.2017.
 */
public enum Platform {

    MOBILE(1, "моб"),
    IPHONE(2, "iPhone"),
    IPAD(3, "iPad"),
    ANDROID(4, "Android"),
    WIN_PHONE(5, "Windows Phone"),
    WIN_10(6, "Windows 10"),
    WEB(7, "web"),
    VK_MOBILE(8, "VK Mobile");

    private final int code;
    private final String name;

    Platform(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }
    public static Platform valueOf(int code) {
        for (Platform st : Platform.values()) {
            if (st.getCode() == code)
                return st;
        }
        return null;
    }
    public String getName() {
        return name;
    }
}
