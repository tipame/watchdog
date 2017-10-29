package ru.tipame.watchdog;

import ru.tipame.watchdog.dto.Config;
import ru.tipame.watchdog.dto.ConfigUser;
import ru.tipame.watchdog.dto.User;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by tipame on 28.10.2017.
 */
public class ViewsWatchDog implements ViewsListener, ThreadFactory, Thread.UncaughtExceptionHandler {

    private VkApiService vkApiService;
    private TelegramService telegramService;
    private ScheduledThreadPoolExecutor executor;

    private Config config;
    private Map<String, ConfigUser> configUserMap = new HashMap<>();

    public ViewsWatchDog(Config config) {

        this.config = config;

        vkApiService = new VkApiService(config);
        telegramService = new TelegramService(config);

        executor = new ScheduledThreadPoolExecutor(1);
        executor.setMaximumPoolSize(2);
        executor.setThreadFactory(this);

        for(ConfigUser user : config.getUsers()) {
            if(user.isDisabled())continue;
            configUserMap.put(user.getId(), user);
            executor.execute(new UserInfoTask(user));
        }
    }

    @Override
    public void hasNewViews(User user, int[] lastViews, int[] newViews) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Активность: %s %s. %s -> %s", user.getFirstName(), user.getLastName(), Arrays.toString(lastViews), Arrays.toString(newViews)));

        ConfigUser configUser = configUserMap.get(user.getAlias());
        ArrayList<String> followers = new ArrayList<>(configUser.getFollowers().length);
        for(ConfigUser follower : configUser.getFollowers()) {
            followers.add(follower.getId());
        }
        List<User> followerUsers = vkApiService.getUsers(followers.toArray(new String[0]));

        boolean hasActive = false;
        for(User follower : followerUsers) {
            if (    follower.isOnline() ||
                    follower.getLastSeen().getTime().getTime() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(config.getActive())
            ) {
                hasActive = true;
                sb.append(System.lineSeparator());
                sb.append("\t");
                sb.append(follower.toString());
            }
        }
        if(configUser.isShowAnyway() || hasActive) {

            String message = sb.toString();

            System.out.println(message);
            System.out.println();

            telegramService.sendMessage(message);
        }
    }

    @Override
    public Thread newThread(Runnable r) {

        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(this);
        t.setDaemon(true);
        return t;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

    private class ViewsWatchTask implements Runnable {

        private User user;
        private ViewsListener listener;

        private int count = 3;
        private int views[] = null;

        private ViewsWatchTask(User user, ViewsListener listener) {
            this.user = user;
            this.listener = listener;
        }

        @Override
        public void run() {

            int currViews[] = vkApiService.getPostsViewCount(user.getId(), count);
            if(views == null) {
                views = currViews;
                return;
            }

            for(int i = 0; i < count; i++) {
                if(views[i] < currViews[i]) {
                    // количество просмотров изменилось
                    listener.hasNewViews(user, views, currViews);
                    break;
                }
            }
            views = currViews;
        }
    }

    private class WatchOnlineTask implements Runnable {

        private User user;
        private boolean online;

        private WatchOnlineTask(User user) {
            this.user = user;
        }

        @Override
        public void run() {

            User curr = vkApiService.getUsers(""+user.getId()).get(0);
            if(curr.isOnline() != online) {
                online = !online;
                String message = String.format("Статус: %s", curr.toString());
                System.out.println(message);
                telegramService.sendMessage(message);
            }
        }
    }

    private class UserInfoTask implements Runnable {

        private ConfigUser configUser;

        private UserInfoTask(ConfigUser configUser) {
            this.configUser = configUser;
        }

        @Override
        public void run() {

            User user = vkApiService.getUsers(configUser.getId()).get(0);

            String message = String.format("%s: %s", configUser.isWatchOnline() ? "Cтатус" : "Просмотры", user.toString());
            System.out.println(message);
            telegramService.sendMessage(message);

            if(configUser.isWatchOnline()) {
                executor.scheduleWithFixedDelay(new WatchOnlineTask(user), 0, config.getDelay(), TimeUnit.SECONDS);
            }
            executor.scheduleWithFixedDelay(new ViewsWatchTask(user, ViewsWatchDog.this), 0, config.getDelay(), TimeUnit.SECONDS);
        }
    }







    public static void main(String[] args) throws IOException, InterruptedException {

        Config config = ConfigParser.parseConfig();
        ViewsWatchDog dog = new ViewsWatchDog(config);
        while(true) {
            Thread.sleep(60000000000l);
        }
    }
}
