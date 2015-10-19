package com.unimelb.monitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.unimelb.data.Record;
import com.unimelb.data.Temperature;
import com.unimelb.utils.mp3Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

public class PreviewLineChartActivity extends FragmentActivity {

    PlaceholderFragment fragment;
    public static Context context;
    private Bundle savedInstanceState;
    @Override
    protected void onCreate(Bundle bundle) {
        context = this;
        this.savedInstanceState = bundle;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_line_chart);
        if (savedInstanceState == null) {
            fragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction() .add(R.id.container, fragment).commit();
        }
    }

    /**
     * A fragment containing a line chart and preview line chart.
     */
    public static class PlaceholderFragment extends Fragment {
        private LineChartView chart;
        private PreviewLineChartView previewChart;
        private LineChartData data;
        private mp3Utils util;

        private String records;
        private double stdDev;
        private double mean;

        /**
         * Deep copy of data.
         */
        private LineChartData previewData;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_preview_line_chart, container, false);
            chart = (LineChartView) rootView.findViewById(R.id.chart);
            previewChart = (PreviewLineChartView) rootView.findViewById(R.id.chart_preview);
            Intent intent = getActivity().getIntent();
            records = intent.getStringExtra("records");
            mean = intent.getDoubleExtra("mean", 0);
            stdDev = intent.getDoubleExtra("stdDev", 0);
            // Generate data for previewed chart and copy of that data for preview chart.
            generateRecordData(records);
//            generateDefaultData();

            chart.setLineChartData(data);
            // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
            // zoom/scroll is unnecessary.
            chart.setZoomEnabled(false);
            chart.setScrollEnabled(false);

            previewChart.setLineChartData(previewData);
            previewChart.setViewportChangeListener(new ViewportListener());

            previewX(false);
            return rootView;
        }

        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.preview_line_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                generateDefaultData();
                chart.setLineChartData(data);
                previewChart.setLineChartData(previewData);
                previewX(true);
                return true;
            }
            if (id == R.id.action_preview_both) {
                previewXY();
                previewChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                return true;
            }
            if (id == R.id.action_preview_horizontal) {
                previewX(true);
                return true;
            }
            if (id == R.id.action_preview_vertical) {
                previewY();
                return true;
            }
            if (id == R.id.action_change_color) {
                int color = ChartUtils.pickColor();
                while (color == previewChart.getPreviewColor()) {
                    color = ChartUtils.pickColor();
                }
                previewChart.setPreviewColor(color);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void generateDefaultData() {
            int numValues = 50;

            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, (float) Math.random() * 100f));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN);
            line.setHasPoints(false);// too many values so don't draw points.

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            data = new LineChartData(lines);
            data.setAxisXBottom(new Axis());
            data.setAxisYLeft(new Axis().setHasLines(true));

            // prepare preview data, is better to use separate deep copy for preview chart.
            // Set color to grey to make preview area more visible.
            previewData = new LineChartData(data);
            previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);

        }

        private void generateRecordData(String jsonStr) {
            List<PointValue> values = new ArrayList<PointValue>();
            List<PointValue> mean_values = new ArrayList<PointValue>();
            List<PointValue> mean_add_stdDev_values = new ArrayList<PointValue>();
            List<PointValue> mean_subtract_stdDev_values = new ArrayList<PointValue>();
            JSONArray jsonArr = null;
            Gson gson = new Gson();
            Map<Float,Float> map = new HashMap<Float,Float>();
            map= (Map<Float, Float>) gson.fromJson(jsonStr, map.getClass());
            Set set = map.entrySet();
            Iterator iterator = set.iterator();
            int secs = 0;
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                float value  = Float.parseFloat(String.valueOf(mentry.getValue()));
                values.add(new PointValue(secs, value));
                mean_values.add(new PointValue(secs, (float) mean));
                mean_add_stdDev_values.add(new PointValue(secs, (float) (mean + stdDev)));
                mean_subtract_stdDev_values.add(new PointValue(secs, (float)(mean - stdDev)));
                secs += 5;
            }
            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN);
            line.setHasPoints(false);// too many values so don't draw points.

            Line line_mean = new Line(mean_values);
            line_mean.setColor(ChartUtils.COLOR_RED);
            line_mean.setHasPoints(false);// too many values so don't draw points.
            line_mean.setFilled(false);

            Line line_mean_add_stdDev = new Line(mean_add_stdDev_values);
            line_mean_add_stdDev.setColor(ChartUtils.COLOR_BLUE);
            line_mean_add_stdDev.setHasPoints(false);// too many values so don't draw points.

            Line line_mean_substract_stdDev = new Line(mean_subtract_stdDev_values);
            line_mean_substract_stdDev.setColor(ChartUtils.COLOR_BLUE);
            line_mean_substract_stdDev.setHasPoints(false);// too many values so don't draw points.

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);
            lines.add(line_mean);
            lines.add(line_mean_add_stdDev);
            lines.add(line_mean_substract_stdDev);

            data = new LineChartData(lines);
            data.setAxisXBottom(new Axis());
            data.setAxisYLeft(new Axis().setHasLines(false));

            // prepare preview data, is better to use separate deep copy for preview chart.
            // Set color to grey to make preview area more visible.
            previewData = new LineChartData(data);
            previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
        }

        private void previewY() {
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            float dy = tempViewport.height() / 4;
            tempViewport.inset(0, dy);
            previewChart.setCurrentViewportWithAnimation(tempViewport);
            previewChart.setZoomType(ZoomType.VERTICAL);
        }

        private void previewX(boolean animate) {
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            float dx = tempViewport.width() / 4;
            tempViewport.inset(dx, 0);
            if (animate) {
                previewChart.setCurrentViewportWithAnimation(tempViewport);
            } else {
                previewChart.setCurrentViewport(tempViewport);
            }
            previewChart.setZoomType(ZoomType.HORIZONTAL);
        }

        private void previewXY() {
            // Better to not modify viewport of any chart directly so create a copy.
            Viewport tempViewport = new Viewport(chart.getMaximumViewport());
            // Make temp viewport smaller.
            float dx = tempViewport.width() / 4;
            float dy = tempViewport.height() / 4;
            tempViewport.inset(dx, dy);
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        }

        /**
         * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
         * viewport of upper chart.
         */
        private class ViewportListener implements ViewportChangeListener {
            @Override
            public void onViewportChanged(Viewport newViewport) {
                // don't use animation, it is unnecessary when using preview chart.
                chart.setCurrentViewport(newViewport);
            }

        }

    }
}
