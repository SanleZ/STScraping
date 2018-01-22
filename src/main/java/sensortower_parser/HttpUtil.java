package sensortower_parser;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Utility class for GET and POST requests
 */
public class HttpUtil {

    public static Response get(URL url, CookieManager cm) throws IOException {
        URLConnection connection;
        boolean isHttps = url.getProtocol().equals("https");

        if (isHttps) {
            connection = (HttpsURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        connection.setConnectTimeout(20000); //20 seconds
        connection.setReadTimeout(40000); //40 seconds

        if (isHttps) {
            ((HttpsURLConnection) connection).setInstanceFollowRedirects(false);
        } else
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);

        if (cm != null) {
            cm.setCookies(connection);
        }

        connection.setRequestProperty("Host", url.getHost());
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.connect();

        if (cm != null) {
            cm.storeCookies(connection);
        }

        int code = isHttps ? ((HttpsURLConnection) connection).getResponseCode() : ((HttpURLConnection) connection).getResponseCode();

        if (code == 200) {
            try (InputStream in = connection.getInputStream()) {
                return new Response(code, readFully(in));
            }
        }
        return new Response(code, new byte[0]);

    }


    public static Response post(URL url, byte[] data, CookieManager cm) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(920000); //20 seconds
        connection.setReadTimeout(940000); //40 seconds
        connection.setInstanceFollowRedirects(false);


        if (cm != null) {
            cm.setCookies(connection);
        }

        connection.setRequestProperty("Host", url.toString());
        connection.setRequestProperty("Content-Length", String.valueOf(data.length));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept-Charset", "text/html,application/xhtml+xml,application/json,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        connection.setRequestProperty("Origin", "https://sensortower.com");
        connection.setRequestProperty("Referer", "https://sensortower.com/");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");

        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        out.write(data);
        out.close();

        int code = connection.getResponseCode();
        if (code == 200) {
            if (cm != null) {
                cm.storeCookies(connection);
            }
            try (InputStream in = connection.getInputStream()) {
                String content = new String(readFully(in), "UTF-8");
                if (content.contains("Invalid email or password")) {
                    return new Response(401, null);
                }
                return new Response(code, null);
            }
        } else if (code == 302) {
            if (cm != null) {
                cm.storeCookies(connection);
            }

            Map<String, List<String>> fields = connection.getHeaderFields();
            String value;
            if (fields.containsKey("Location") && !fields.get("Location").isEmpty()
                    || (fields.containsKey("location") && !fields.get("location").isEmpty())) {
                if (fields.containsKey("Location")) {
                    value = fields.get("Location").get(0);
                } else {
                    value = fields.get("location").get(0);
                }
                URL location = new URL(value);
                return get(location, cm);
            }
        }
        return new Response(code, null);
    }

    /**
     * Reads all the bytes from an input stream.
     */
    private static byte[] readFully(InputStream source) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[10 * 1024];
        int n;
        while ((n = source.read(buf)) > 0) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

}
