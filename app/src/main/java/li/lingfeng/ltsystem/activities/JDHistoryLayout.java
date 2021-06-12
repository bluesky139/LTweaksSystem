package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ShoppingUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

import static li.lingfeng.ltsystem.utils.ContextUtils.dp2px;

public class JDHistoryLayout extends RelativeLayout implements
        OnChartGestureListener, OnChartValueSelectedListener {

    private Activity mActivity;
    private ProgressBar mProgressBar;
    private LineChart mChart;
    private PriceHistoryGrabber mGrabber;
    private PriceHistoryGrabber.Result mData;
    private DecimalFormat mDec = new DecimalFormat("#,###.00");

    public JDHistoryLayout(Activity activity) {
        super(activity);
        mActivity = activity;

        mProgressBar = new ProgressBar(activity);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(mProgressBar, layoutParams);

        mChart = new LineChart(activity);
        mChart.setVisibility(View.INVISIBLE);
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp2px(16), dp2px(40), dp2px(16), dp2px(40));
        addView(mChart, layoutParams);

        boolean ok = findItemIdAndGrabHistory();
        if (!ok) {
            exit("findItemIdAndGrabHistory return false.");
        }
    }

    private boolean findItemIdAndGrabHistory() {
        Object _itemId = mActivity.getIntent().getExtras().get("id");
        String itemId = _itemId != null ? _itemId.toString() : null;
        Logger.d("itemId " + itemId);
        if (StringUtils.isEmpty(itemId)) {
            Logger.intent(mActivity.getIntent());
            return false;
        }
        @ShoppingUtils.Store int store = ShoppingUtils.STORE_JD;

        mGrabber = new PriceHistoryGrabber(store, itemId,
                new PriceHistoryGrabber.GrabCallback() {
            @Override
            public void onResult(final PriceHistoryGrabber.Result result) {
                Logger.i("Prices result " + result);
                mActivity.runOnUiThread(() -> {
                    if (result == null) {
                        exit(ContextUtils.getLString(R.string.jd_history_can_not_get_prices));
                        return;
                    }
                    try {
                        mData = result;
                        createChart(result);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mChart.setVisibility(View.VISIBLE);
                    } catch (Throwable e) {
                        Logger.e("Create chart exception.", e);
                    }
                });
            }

            @Override
            public void onRedirect(String url) {
                Logger.i("Redirect url " + url + ", should be recaptcha.");
                mActivity.runOnUiThread(() -> {
                    if (url == null) {
                        exit(ContextUtils.getLString(R.string.jd_history_can_not_get_prices));
                        return;
                    }
                    WebView webView = new WebView(mActivity);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            Uri uri = request.getUrl();
                            Logger.d("shouldOverrideUrlLoading " + uri);
                            if (uri.getHost().endsWith("jd.com")) {
                                String cookie = CookieManager.getInstance().getCookie("https://browser.gwdang.com");
                                Logger.d("Got cookie " + cookie);
                                mActivity.getSharedPreferences("ltweaks", 0).edit().putString("history_cookie", cookie).apply();
                                ViewUtils.removeView(webView);
                                mGrabber.setCookie(cookie);
                                mGrabber.startRequest();
                                return true;
                            }
                            return false;
                        }
                    });
                    addView(webView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    webView.loadUrl(url);
                });
            }
        });

        String cookie = mActivity.getSharedPreferences("ltweaks", 0).getString("history_cookie", null);
        if (cookie != null) {
            Logger.d("Has cookie " + cookie);
            mGrabber.setCookie(cookie);
        }
        mGrabber.startRequest();
        return true;
    };

    private void exit(String msg) {
        Logger.i("Exit msg, " + msg);
        mActivity.runOnUiThread(() -> Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show());
    }

    private void createChart(final PriceHistoryGrabber.Result data) {
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(mActivity, R.layout.jd_history_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        /*LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(18f);*/

        mChart.setExtraTopOffset(10f);
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum((data.endTime - data.startTime) / 24f / 3600f);
        xAxis.setLabelCount(3);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                value *= 24 * 3600;
                value += data.startTime;
                //Timestamp timestamp = new Timestamp();
                Date date = new Date(((long) value) * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                //Log.d("test", "getFormattedValue " + ((long) value) + ", " + dateFormat.format(date));
                return dateFormat.format(date);
            }
        });
        xAxis.setTextSize(12f);
        xAxis.setTextColor(0xFFFAFAFA);
        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


        LimitLine ll1 = new LimitLine(data.maxPrice, ContextUtils.getLString(R.string.jd_history_highest) + ": " + mDec.format(data.maxPrice));
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(14f);
        ll1.setTextColor(Color.RED);

        LimitLine ll2 = new LimitLine(data.minPrice, ContextUtils.getLString(R.string.jd_history_lowest) + ": " + mDec.format(data.minPrice));
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(14f);
        ll2.setTextColor(Color.RED);

        float axisHead = (data.maxPrice - data.minPrice) / 5f;
        axisHead = axisHead == 0f ? 20f : axisHead;
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(data.maxPrice + axisHead);
        leftAxis.setAxisMinimum(data.minPrice - axisHead);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(0xFFFAFAFA);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setData(data);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(1000);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(12f);
        l.setTextColor(0xFFFAFAFA);
        //l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    private void setData(PriceHistoryGrabber.Result data) {

        ArrayList<Entry> values = new ArrayList<Entry>();
        for (int i = 0; i < data.prices.size(); i++) {

            float val = data.prices.get(i);
            values.add(new Entry(i, val));
        }

        LineDataSet set1;
        // create a dataset and give it a type
        set1 = new LineDataSet(values, ContextUtils.getLString(R.string.app_name));

        // set the line to be drawn like this "- - - - - -"
        //set1.enableDashedLine(10f, 5f, 0f);
        //set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(ContextUtils.getLColor(R.color.jd_history_main_line));
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(14f);
        set1.setDrawFilled(false);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.STEPPED);
        set1.setFormLineWidth(1f);
        //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        Drawable drawable = ContextUtils.getLDrawable(R.drawable.jd_history_fade_red);
        set1.setFillDrawable(drawable);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        // set data
        mChart.setData(new LineData(dataSets));
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        //Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        //Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public class MyMarkerView extends MarkerView {

        private TextView tvContent;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            float x = e.getX();
            x *= 24 * 3600;
            x += mData.startTime;
            Date date = new Date(((long) x) * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            //Log.d("test", "getFormattedValue " + ((long) x) + ", " + dateFormat.format(date));
            tvContent.setText(mDec.format(e.getY()) + "\n" + dateFormat.format(date));

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            //return new MPPointF(0f, 0f);
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }

        /*@Override
        public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
            return new MPPointF(0f, 0f);
        }*/

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            super.draw(canvas, posX, 200f);
        }
    }
}
