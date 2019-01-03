package li.lingfeng.ltsystem.tweaks.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.SHADOWSOCKS, prefs = {})
public class ShadowsocksPrimaryDns extends TweakBase {

    @Override
    public void java_lang_ProcessBuilder__start__(ILTweaks.MethodParam param) {
        param.before(() -> {
            ProcessBuilder processBuilder = (ProcessBuilder) param.thisObject;
            if (processBuilder.command().get(0).endsWith("liboverture.so")) {
                String value = Prefs.instance().getString(R.string.key_shadowsocks_primary_dns, "");
                String[] dnsArray = StringUtils.split(value, ',');
                if (dnsArray.length == 0) {
                    return;
                }
                Logger.i("Modify primary dns, " + value);

                File file = new File("/data/user_de/0/" + PackageNames.SHADOWSOCKS + "/files/overture.conf");
                String content = FileUtils.readFileToString(file);
                JSONObject jContent = (JSONObject) JSON.parse(content, Feature.OrderedField);
                JSONArray jDnsArray = jContent.getJSONArray("PrimaryDNS");
                JSONObject jDnsTemplate = jContent.getJSONArray("PrimaryDNS").getJSONObject(0);
                jDnsArray.clear();

                for (String dns : dnsArray) {
                    JSONObject jDns = (JSONObject) jDnsTemplate.clone();
                    jDns.put("Name", "Primary-" + dns);
                    jDns.put("Address", dns);
                    jDnsArray.add(jDns);
                }
                FileUtils.writeStringToFile(file, JSON.toJSONString(jContent, SerializerFeature.DisableCircularReferenceDetect));
            }
        });
    }
}
