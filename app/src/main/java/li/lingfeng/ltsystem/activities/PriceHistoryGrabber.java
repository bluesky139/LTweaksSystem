package li.lingfeng.ltsystem.activities;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ShoppingUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by smallville on 2016/11/22.
 */
public class PriceHistoryGrabber {

    public interface GrabCallback {
        void onResult(Result result);
        void onRedirect(String url);
    }

    public class Result {
        public int startTime;
        public int endTime;
        public List<Float> prices = new ArrayList<>();
        public float minPrice = Float.MAX_VALUE;
        public float maxPrice = 0f;
    }

    private String mItemId;
    private String mUrl = "https://browser.gwdang.com/extension?ac=price_trend&dp_ids=&dp_id=%s-%d&price=&format=json&union=union_gwdang&version=1478246552639&from_device=chrome&crc64=1";
    private String mCookie;
    private OkHttpClient client = new OkHttpClient();
    private GrabCallback mGrabCallback;

    public PriceHistoryGrabber(@ShoppingUtils.Store int store, String itemId, GrabCallback callback) {
        mItemId = itemId;
        mUrl = String.format(mUrl, itemId, store);
        mGrabCallback = callback;
    }

    public void setCookie(String cookie) {
        mCookie = cookie;
    }

    public void startRequest() {
        try {
            Logger.i("Start request " + mUrl);
            Request.Builder builder = new Request.Builder()
                    .url(mUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36")
                    .header("Referer", "https://item.jd.com/" + mItemId + ".html");
            if (mCookie != null) {
                builder.header("Cookie", mCookie);
            }
            Request request = builder.build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e("onFailure " + e);
                    mGrabCallback.onResult(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        Logger.i("Got prices, parsing...");
                        Result result = parseResponse(response.body().string());
                        mGrabCallback.onResult(result);
                    } else if (response.code() == 202) {
                        String body = response.body().string();
                        Logger.d("Redirect response, " + body);
                        String url = parseRedirectResponse(body);
                        mGrabCallback.onRedirect(url);
                    } else {
                        Logger.e("onResponse " + response);
                        mGrabCallback.onResult(null);
                    }
                }
            });
        } catch (Exception e) {
            Logger.e("PriceHistoryGrabber request exception, " + e.getMessage());
            mGrabCallback.onResult(null);
        }
    }

    private Result parseResponse(String text) {
        try {
            JSONObject all = (JSONObject) JSON.parse(text);
            JSONArray stores = (JSONArray) all.get("store");
            JSONObject jdStore = (JSONObject) stores.get(0);
            Result result = new Result();

            result.startTime = (int) (Long.parseLong(jdStore.get("all_line_begin_time").toString()) / 1000);
            result.endTime = Integer.parseInt(jdStore.get("max_stamp").toString());

            List prices = (List) jdStore.get("all_line");
            if (prices.size() == 0) {
                Logger.i("No history prices.");
                return null;
            }
            for (Object price_ : prices) {
                float price = Float.parseFloat(price_.toString());
                result.prices.add(price);
                if (price > result.maxPrice) {
                    result.maxPrice = price;
                }
                if (price < result.minPrice) {
                    result.minPrice = price;
                }
            }
            return result;
        } catch (Exception e) {
            Logger.e("Failed to parse json, " + e.getMessage());
            return null;
        }
    }

    private String parseRedirectResponse(String text) {
        try {
            return JSON.parseObject(text).getJSONObject("action").getString("to");
        } catch (Throwable e) {
            Logger.e("Failed to parse redirect response, ", e);
            return null;
        }
    }
}
