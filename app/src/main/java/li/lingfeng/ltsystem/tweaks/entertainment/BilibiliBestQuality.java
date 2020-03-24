package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_best_quality)
public class BilibiliBestQuality extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";
    private static final String LIVE_ROOM_ACTIVITY = "com.bilibili.bililive.videoliveplayer.ui.roomv3.LiveRoomActivityV3";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Uri uri = activity.getIntent().getData();
            //Logger.d("uri " + uri);
            if (uri == null) {
                return;
            }

            String preload = uri.getQueryParameter("player_preload");
            preload = Uri.decode(preload);
            JSONObject jPreload = JSON.parseObject(preload);
            if (jPreload == null) {
                return;
            }
            int quality = jPreload.getIntValue("quality");
            int bestQuality = quality;
            JSONArray jVideos = jPreload.getJSONObject("dash").getJSONArray("video");
            for (Object _jVideo : jVideos) {
                JSONObject jVideo = (JSONObject) _jVideo;
                bestQuality = Math.max(bestQuality, jVideo.getIntValue("id"));
            }

            if (bestQuality != quality) {
                Logger.v("Change default quality for half screen play " + quality + " -> " + bestQuality);
                jPreload.put("quality", bestQuality);
                preload = Uri.encode(jPreload.toString());

                Uri.Builder builder = uri.buildUpon().clearQuery();
                for (String key : uri.getQueryParameterNames()) {
                    if (key.equals("player_preload")) {
                        builder.appendQueryParameter("player_preload", preload);
                    } else {
                        builder.appendQueryParameter(key, uri.getQueryParameter(key));
                    }
                }
                uri = builder.build();
                //Logger.d("new uri: " + uri);
                activity.getIntent().setData(uri);
            }
        });
        beforeOnClass(LIVE_ROOM_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            String qualities = intent.getStringExtra("quality_description");
            if (qualities != null) {
                Logger.d("quality_description " + qualities);
                JSONArray jArray = JSON.parseArray(qualities);
                String maxQuality = jArray.getJSONObject(0).getString("qn");
                Logger.d("best quality " + maxQuality);
                intent.putExtra("current_qn", maxQuality);
            }
        });
    }
}
