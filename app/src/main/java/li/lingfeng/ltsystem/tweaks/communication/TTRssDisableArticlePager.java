package li.lingfeng.ltsystem.tweaks.communication;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TT_RSS, prefs = R.string.key_ttrss_disable_article_pager)
public class TTRssDisableArticlePager extends TweakBase {

    private static final String DETAIL_ACTIVITY = "org.fox.ttrss.DetailActivity";
    private static final String ARTICLE_PAGER = "org.fox.ttrss.ArticlePager";
    private static final String ARTICLE_LIST = "org.fox.ttrss.types.ArticleList";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(DETAIL_ACTIVITY, param, () -> {
            Object fragmentManager = ReflectUtils.callMethod(param.thisObject, "getSupportFragmentManager");
            List pendingActions = (List) ReflectUtils.getObjectField(fragmentManager, "mPendingActions");
            List ops = (List) ReflectUtils.getObjectField(pendingActions.get(0), "mOps");
            Object fragment = ops.stream()
                    .map(op -> {
                        try {
                            return ReflectUtils.getObjectField(op, "fragment");
                        } catch (Throwable e) {
                            return null;
                        }
                    })
                    .filter(f -> f != null && f.getClass().getName().equals(ARTICLE_PAGER)).findFirst().get();

            Object article = ReflectUtils.getObjectField(fragment, "m_article");
            List articleList = (List) findClass(ARTICLE_LIST).newInstance();
            articleList.add(article);
            ReflectUtils.setObjectField(fragment, "m_articles", articleList);
        });
    }
}
