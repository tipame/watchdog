package ru.tipame.watchdog;

import ru.tipame.watchdog.dto.User;

/**
 * Created by tipame on 28.10.2017.
 */
public interface ViewsListener {

    void hasNewViews(User user, int[] lastViews, int[] newViews);
}
