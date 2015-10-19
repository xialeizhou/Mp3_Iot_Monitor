package com.unimelb.monitor;

/**
 * Created by xialeizhou on 9/17/15.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.unimelb.utils.Statistics;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.BubbleChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.BubbleChartData;
import lecho.lib.hellocharts.model.BubbleValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.BubbleChartView;
import lecho.lib.hellocharts.view.Chart;

public class BubbleChartActivity extends FragmentActivity {
    PlaceholderFragment fragment;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_chart);
        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
            fragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction() .add(R.id.container, fragment).commit();
        }
    }

    /**
     * A fragment containing a bubble chart.
     */
    public static class PlaceholderFragment extends Fragment {

        private BubbleChartView chart;
        private BubbleChartData data;
        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private ValueShape shape = ValueShape.CIRCLE;
        private boolean hasLabels = false;
        private boolean hasLabelForSelected = false;
        private static final double GOLDEN_VALUE = new Double("330");
        private static final double GOLDEN_VALUE_STEP = new Double("10");

        private String records;
        private double meanVal;
        private double step;
        private double stdDev;
        private double variance;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_bubble_chart, container, false);

            chart = (BubbleChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());

            Intent intent = getActivity().getIntent();
            records = intent.getStringExtra("records");
            meanVal = intent.getDoubleExtra("mean", GOLDEN_VALUE);
            step = intent.getDoubleExtra("step", GOLDEN_VALUE_STEP);
            stdDev = intent.getDoubleExtra("stdDev", 0);
            generateData(records);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.bubble_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData(records);
                return true;
            }
            if (id == R.id.action_shape_circles) {
                setCircles();
                return true;
            }
            if (id == R.id.action_shape_square) {
                setSquares();
                return true;
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_toggle_axes) {
                toggleAxes();
                return true;
            }
            if (id == R.id.action_toggle_axes_names) {
                toggleAxesNames();
                return true;
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation();
                chart.startDataAnimation();
                return true;
            }
            if (id == R.id.action_toggle_selection_mode) {
                toggleLabelForSelected();
                Toast.makeText(getActivity(),
                        "Selection mode set to " + chart.isValueSelectionEnabled() + " select any point.",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == R.id.action_toggle_touch_zoom) {
                chart.setZoomEnabled(!chart.isZoomEnabled());
                Toast.makeText(getActivity(), "IsZoomEnabled " + chart.isZoomEnabled(), Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == R.id.action_zoom_both) {
                chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
                return true;
            }
            if (id == R.id.action_zoom_horizontal) {
                chart.setZoomType(ZoomType.HORIZONTAL);
                return true;
            }
            if (id == R.id.action_zoom_vertical) {
                chart.setZoomType(ZoomType.VERTICAL);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void reset() {
            hasAxes = true;
            hasAxesNames = true;
            shape = ValueShape.CIRCLE;
            hasLabels = false;
            hasLabelForSelected = false;

            chart.setValueSelectionEnabled(hasLabelForSelected);
        }

        private void generateData(String jsonStr) {
            List<BubbleValue> values = new ArrayList<BubbleValue>();
            Gson gson = new Gson();
            Map<Float,Float> map = new HashMap<Float,Float>();
            map= (Map<Float, Float>) gson.fromJson(jsonStr, map.getClass());
            Set set = map.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                float date = Float.parseFloat(String.valueOf(mentry.getKey()));
                float value  = Float.parseFloat(String.valueOf(mentry.getValue()));
                int level = getLevel(value);
                List<Float> lnglat = getLngLat(value, level);
                BubbleValue bbval = new BubbleValue(lnglat.get(0), lnglat.get(1), (float) Math.random() * 1000);
                bbval.setLabel("level:" + level + ",value:" + value + ",secs:" + String.valueOf(date));
                if ( level <= 10) {
                    bbval.setShape(ValueShape.CIRCLE);
                    bbval.setColor(ChartUtils.COLOR_GREEN); // good point
                } else {
                    bbval.setShape(ValueShape.SQUARE);
                    bbval.setColor(ChartUtils.COLOR_RED); // bad point
                }
                values.add(bbval);
            }
            data = new BubbleChartData(values);
            data.setHasLabels(hasLabels);
            data.setHasLabelsOnlyForSelected(hasLabelForSelected);
            data.setBubbleScale((float) 0.000001); // scale radius

            if (hasAxes) {
//                Axis axis_x_left = new Axis();
//                Axis axis_x_right = new Axis();
                Axis axis_x_bottom = new Axis();
                Axis axis_y_left = new Axis();
//                Axis axisY = new Axis().setHasLines(true);
//                Axis axisY = new Axis().setInside(true);
//                Axis axisY = new Axis().setHasSeparationLine(true);
                if (hasAxesNames) {
//                    axis_x_bottom.setName("mean:"+meanVal+",stdDev:"+stdDev+",step:"+step);
//                    axis_x_bottom.setName("mean:"+meanVal+",stdDev:"+stdDev);
//                    axis_y_left.setName("Level(Good:1-10, Bad:11-18)");
//                    axisY.setName("Axis Y");
                }
//                data.setAxisYLeft(axis_y_left);
//                data.setAxisXBottom(axis_x_bottom);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }
            chart.setBubbleChartData(data);

        }

        private void setCircles() {
            shape = ValueShape.CIRCLE;
            generateData(records);
        }

        private void setSquares() {
            shape = ValueShape.SQUARE;
            generateData(records);
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                chart.setValueSelectionEnabled(hasLabelForSelected);
            }

            generateData(records);
        }

        private void toggleLabelForSelected() {
            hasLabelForSelected = !hasLabelForSelected;

            chart.setValueSelectionEnabled(hasLabelForSelected);

            if (hasLabelForSelected) {
                hasLabels = false;
            }

            generateData(records);
        }

        private void toggleAxes() {
            hasAxes = !hasAxes;

            generateData(records);
        }

        private List<Float> getLngLat(float value, int level) {
            List<Float> lnglat = new ArrayList<Float>();
            Random rand = new Random();
            float min = 0;
            float max = level;
            float lng = rand.nextFloat() * (max - min) + min;
            float abslng = Math.abs(lng);

            min = -abslng + level - 1;
            max = -abslng + level;
            float lat = rand.nextFloat() * (max - min) + min;
            float abslat = Math.abs(lat);
            lnglat.add(abslng);
            lnglat.add(abslat);
            return lnglat;
        }

        private int getLevel(float value) {
            float absVal = Math.abs(value);
            float absMean = (float)Math.abs(meanVal);
            if (Math.abs(absMean - absVal) < step) {
                return 10;
            } else if (Math.abs(absMean - absVal) < step * 2) {
                return 9;
            } else if (Math.abs(absMean - absVal) < step * 3) {
                return 8;
            } else if (Math.abs(absMean - absVal) < step * 4) {
                return 7;
            } else if (Math.abs(absMean - absVal) < step * 5) {
                return 11;
            } else if (Math.abs(absMean - absVal) < step * 6) {
                return 12;
            } else if (Math.abs(absMean - absVal) < step * 7) {
                return 13;
            } else if (Math.abs(absMean - absVal) < step * 8) {
                return 14;
            }
            return 15;
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;
            generateData(records);
        }

        /**
         * To animate values you have to change targets values and then call {@link Chart#startDataAnimation()}
         * method(don't confuse with View.animate()).
         */
        private void prepareDataAnimation() {
            for (BubbleValue value : data.getValues()) {
                value.setTarget(value.getX() + (float) Math.random() * 4 * getSign(), (float) Math.random() * 100,
                        (float) Math.random() * 1000);
            }
        }

        private int getSign() {
            int[] sign = new int[]{-1, 1};
            return sign[Math.round((float) Math.random())];
        }

        private class ValueTouchListener implements BubbleChartOnValueSelectListener {

            @Override
            public void onValueSelected(int bubbleIndex, BubbleValue value) {
                //Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
                String label = String.copyValueOf(value.getLabel());
                Toast.makeText(getActivity(), label, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }
        }
    }
}
