package sensortower_parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class Scraper {

    private static CookieManager cookieManager = new CookieManager();

    private static boolean login(final String email, final String password) throws Exception {

        String loginUrl = "https://sensortower.com/users/sign_in";
        StringBuilder requestDataBuilder = new StringBuilder()
                .append("return_to_path=%2F")
                .append("&user%5Bemail%5D=").append(URLEncoder.encode(email, "UTF-8"))
                .append("&user%5Bpassword%5D=").append(URLEncoder.encode(password, "UTF-8"));

        Response response = HttpUtil.post(new URL(loginUrl), requestDataBuilder.toString().getBytes("UTF-8"), cookieManager);
        if (response.getCode() != 200) {
            System.out.println("Login Error, HTTP status code =  " + response.getCode());
        }
        return (response.getCode() == 200);
    }

    private static String research(final String keyword, final String countryCode) throws Exception {
        StringBuilder urlSb = new StringBuilder()
                .append("https://sensortower.com/api/android/ajax/research_keyword?page_index=0&term=")
                .append(URLEncoder.encode(keyword,"UTF-8"))
                .append("&country=").append(URLEncoder.encode(countryCode,"UTF-8"))
                .append("&realtime=true");

        Response response = HttpUtil.get(new URL(urlSb.toString()), cookieManager);

        if (response.getCode() == 200) {
            return parse(response);
        }
        return "Error research, HTTP status code = " + response.getCode();
    }

    private static String parse(Response response) throws UnsupportedEncodingException {
        JsonObject jsonCoreObject = new JsonParser().parse(new String(response.getBody(), "UTF-8")).getAsJsonObject();
        JsonObject jKeywordObj = jsonCoreObject.get("keyword").getAsJsonObject();
        StringBuilder sb = new StringBuilder();

        JsonElement trafficJElm = jKeywordObj.get("traffic");
        JsonElement difficultyJElm = jKeywordObj.get("phone_apps").getAsJsonObject().get("difficulty");
        JsonElement appsJElm = jKeywordObj.get("phone_apps").getAsJsonObject().get("app_list_size");

        String traffic = trafficJElm.isJsonNull() ? "-" : trafficJElm.getAsString();
        String difficulty = difficultyJElm.isJsonNull() ? "-" : difficultyJElm.getAsString();
        String apps = appsJElm.isJsonNull() ? "-" : appsJElm.getAsString();
        sb.append("Traffic=").append(traffic).append(", ")
                .append("Difficulty=").append(difficulty).append(", ")
                .append("Apps=").append(apps);
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (Scraper.login("", "")) {
            System.out.println(Scraper.research("my key", "US"));
            System.out.println(Scraper.research("keyword", "US"));
            System.out.println(Scraper.research("saturday", "FR"));
        }
    }
}
