package ru.tipame.watchdog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import ru.tipame.watchdog.dto.Config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tipame on 28.10.2017.
 */
public class TelegramService {

    private Charset charset = Charset.forName("UTF-8");
    private HttpParams params;

    private static final String URL_SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage";
    private static final String PARAM_CHAT_ID = "chat_id";
    private static final String PARAM_TEXT = "text";

    private Config config;
    private String url;

    public TelegramService(Config config) {

        this.config = config;
        this.url = String.format(URL_SEND_MESSAGE, config.getTelegramToken());

        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        params.setIntParameter(CoreConnectionPNames.SO_LINGER, 0);
        params.setBooleanParameter(CoreConnectionPNames.SO_REUSEADDR, true);
        params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 1024);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        params.setIntParameter(CoreConnectionPNames.MAX_HEADER_COUNT, 15);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    public void sendMessage(String text) {

        if(!config.isSendTelegram()) {
            return;
        }

        HttpPost post = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(PARAM_CHAT_ID, config.getTelegramChat()));
        nvps.add(new BasicNameValuePair(PARAM_TEXT, text));
        post.setEntity(new UrlEncodedFormEntity(nvps, charset));

        HttpClient httpClient = new DefaultHttpClient(params);
        try {
            HttpResponse response = httpClient.execute(post);
            if(response.getStatusLine().getStatusCode() != 200) {
                System.out.println("HTTP RESPONSE CODE: " + response.getStatusLine().getStatusCode());
            }
            String responseData = EntityUtils.toString(response.getEntity(), charset);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to execute getPostById()");
        }
    }











    public static void main(String[] args) throws IOException, InterruptedException {

        Config config = ConfigParser.parseConfig();
        TelegramService telegramService = new TelegramService(config);
        telegramService.sendMessage("Hi man!");
        Thread.sleep(60000000);
    }
}
