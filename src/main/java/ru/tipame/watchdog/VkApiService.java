package ru.tipame.watchdog;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import ru.tipame.watchdog.dto.Config;
import ru.tipame.watchdog.dto.Post;
import ru.tipame.watchdog.dto.User;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by tipame on 28.10.2017.
 */
public class VkApiService implements ThreadFactory, Thread.UncaughtExceptionHandler {

    private static final String PARAM_ACCESS_TOKEN = "access_token";

    private static final String PARAM_API_VERSION = "v";
    private static final String API_VERSION = "5.68";

    private static final String URL_WALL_GET_BY_ID = "https://api.vk.com/method/wall.getById";
    private static final String PARAM_POST_ID = "posts";

    private static final String URL_WALL_GET = "https://api.vk.com/method/wall.get";
    private static final String PARAM_OWNER_ID = "owner_id";
    private static final String PARAM_COUNT = "count";

    private static final String URL_USERS_GET = "https://api.vk.com/method/users.get";
    private static final String PARAM_USER_IDS = "user_ids";
    private static final String PARAM_FIELDS = "fields";
    private static final String PARAM_FIELDS_VALUE = "id,first_name,last_name,last_seen,online,screen_name";

    private Charset charset = Charset.forName("UTF-8");
    private HttpParams params;

    private LinkedBlockingQueue<FutureTask> queue = new LinkedBlockingQueue<FutureTask>();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private Config config;

    public VkApiService(Config config) {

        this.config = config;

        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        params.setIntParameter(CoreConnectionPNames.SO_LINGER, 0);
        params.setBooleanParameter(CoreConnectionPNames.SO_REUSEADDR, true);
        params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 1024);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        params.setIntParameter(CoreConnectionPNames.MAX_HEADER_COUNT, 15);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

        executor.setMaximumPoolSize(1);
        executor.setThreadFactory(this);
        executor.scheduleWithFixedDelay(new QueueTask(), 0, 1, TimeUnit.SECONDS);
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

    private String executeRequest(HttpPost request) {

        try {
            FutureTask<String> task = new FutureTask<>(new RequestCallable(request));
            queue.put(task);
            String response = task.get(10, TimeUnit.SECONDS);
            if(response == null) {
                throw new RuntimeException("Fail to get response from VK in timeout");
            }
            return response;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to executeRequest", e);
        }
    }


    private class QueueTask implements Runnable {

        @Override
        public void run() {
            try {
                FutureTask task = queue.take();
                task.run();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class RequestCallable implements Callable<String> {

        private HttpPost httpPost;

        private RequestCallable(HttpPost httpPost) {
            this.httpPost = httpPost;
        }

        @Override
        public String call() throws Exception {

            HttpClient httpClient = new DefaultHttpClient(params);
            try {
                HttpResponse response = httpClient.execute(httpPost);
                if(response.getStatusLine().getStatusCode() != 200) {
                    System.out.println("HTTP RESPONSE CODE: " + response.getStatusLine().getStatusCode());
                }
                String responseData = EntityUtils.toString(response.getEntity(), charset);
                return responseData;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Fail to execute getPostById()");
            }
        }
    }

    public String getPostById(String postId) {

        HttpPost post = new HttpPost(URL_WALL_GET_BY_ID);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(PARAM_POST_ID, postId));
        nvps.add(new BasicNameValuePair(PARAM_ACCESS_TOKEN, config.getVkToken()));
        nvps.add(new BasicNameValuePair(PARAM_API_VERSION, API_VERSION));
        post.setEntity(new UrlEncodedFormEntity(nvps, charset));

        return executeRequest(post);
    }

    public String getPostsByUid(int userId, int count) {

        HttpPost post = new HttpPost(URL_WALL_GET);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(PARAM_OWNER_ID, ""+userId));
        nvps.add(new BasicNameValuePair(PARAM_COUNT, ""+count));
        nvps.add(new BasicNameValuePair(PARAM_ACCESS_TOKEN, config.getVkToken()));
        nvps.add(new BasicNameValuePair(PARAM_API_VERSION, API_VERSION));
        post.setEntity(new UrlEncodedFormEntity(nvps, charset));

        return executeRequest(post);
    }

    public String getUser(String...userIds) {

        HttpPost post = new HttpPost(URL_USERS_GET);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(PARAM_USER_IDS, StringUtils.join(userIds, ',')));
        nvps.add(new BasicNameValuePair(PARAM_FIELDS, PARAM_FIELDS_VALUE));
        nvps.add(new BasicNameValuePair(PARAM_ACCESS_TOKEN, config.getVkToken()));
        nvps.add(new BasicNameValuePair(PARAM_API_VERSION, API_VERSION));
        post.setEntity(new UrlEncodedFormEntity(nvps, charset));

        return executeRequest(post);
    }

    public int getPostViewCount(String postId) {

        String res = getPostById(postId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode root = mapper.readTree(res);
            ArrayNode entries = (ArrayNode) root.path("response");
            Iterator<JsonNode> iterator = entries.elements();
            if(iterator.hasNext()) {
                Post post = mapper.readValue(iterator.next().traverse(), Post.class);
                return post.getViews().getCount();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to parse getPostViewCount()");
        }
        return -1;
    }

    public int[] getPostsViewCount(int userId, int count) {

        String res = getPostsByUid(userId, count);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        int[] counts = new int[count];
        try {
            JsonNode root = mapper.readTree(res);
            ArrayNode entries = (ArrayNode) root.path("response").path("items");

            Iterator<JsonNode> iterator = entries.elements();
            int i = 0;
            while(iterator.hasNext()) {
                Post post = mapper.readValue(iterator.next().traverse(), Post.class);
                counts[i++] = post.getViews().getCount();
            }
            return counts;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to parse getPostsViewCount()");
        }
    }

    public List<User> getUsers(String...userIds) {

        String res = getUser(userIds);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<User> users = new ArrayList<User>();
        try {
            JsonNode root = mapper.readTree(res);
            ArrayNode entries = (ArrayNode) root.path("response");

            Iterator<JsonNode> iterator = entries.elements();
            while(iterator.hasNext()) {
                User user = mapper.readValue(iterator.next().traverse(), User.class);
                users.add(user);
            }
            return users;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to parse getUsers()");
        }
    }






    public static void main(String[] args) throws IOException, InterruptedException {

        Config config = ConfigParser.parseConfig();
        int count = new VkApiService(config).getPostViewCount("2637823_2223");
        System.out.println("COUNT = "+count);

        //int[] counts = new VkApiService().getPostsViewCount("8543652", 2);
        //System.out.println("COUNTS = "+ Arrays.toString(counts));

        //List<User> users = new VkApiService().getUsers("idlampa16", "broondulyak", "8543652", "3804770");
        //System.out.println("USERS = "+ users);

        //Config config = ConfigParser.parseConfig();
        //ViewsWatchDog dog = new ViewsWatchDog(config);
        //Thread.sleep(60000000000l);
    }

    // 2637823_2223   "8543652_1225"
}
