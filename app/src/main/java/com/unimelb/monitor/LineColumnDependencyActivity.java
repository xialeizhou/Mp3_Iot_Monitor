package com.unimelb.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class LineColumnDependencyActivity extends FragmentActivity {

    PlaceholderFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);
        if (savedInstanceState == null) {
            fragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction() .add(R.id.container, fragment).commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
//        public final static String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
//                "Sep", "Oct", "Nov", "Dec",};
        public final static String[] months = new String[]{"1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h",
                "9h", "10h", "11h", "12h"};

        public final static String[] days = new String[]{"0m","1m", "2m", "3m", "4m", "5m","6m","7m","8m","9m","10m","11m","12m"};

        private LineChartView chartTop;
        private ColumnChartView chartBottom;

        private LineChartData lineData;
        private ColumnChartData columnData;

        private String records;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            // *** TOP LINE CHART ***
            chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);

            Intent intent = getActivity().getIntent();
            records = intent.getStringExtra("records");

            // Generate and set data for line chart
            generateInitialLineData();

            // *** BOTTOM COLUMN CHART ***

            chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);

//            generateColumnData();
            generateColumnDataNew(records);

            return rootView;
        }

        private void generateColumnData() {

            int numSubcolumns = 1;
            int numColumns = months.length;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {
                values = new ArrayList<SubcolumnValue>();
                 for (int j = 0; j < numSubcolumns; ++j) {
                     values.add(new SubcolumnValue(10f, ChartUtils.pickColor()));
                 }

                axisValues.add(new AxisValue(i).setLabel(months[i]));

                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }

            columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));

            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);
        }

        private void generateColumnDataNew(String jsonStr) {
            int numSubcolumns = 1;
            int numColumns = months.length;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;

            Gson gson = new Gson();
            Type typeOfHashMap = new TypeToken<Map<Integer, List<Float>>>() { }.getType();
            Map<Integer,List<Float>> map = gson.fromJson(jsonStr, typeOfHashMap);

            Set set = map.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                values = new ArrayList<SubcolumnValue>();
                Map.Entry mentry = (Map.Entry) iterator.next();
                Integer colIdx = Integer.parseInt(String.valueOf(mentry.getKey()));
                List<Float> subColData = (List<Float>)mentry.getValue();
                Float colSize = (float)subColData.size();
                for (int j = 0; j < numSubcolumns; ++j) {
                    values.add(new SubcolumnValue(colSize, ChartUtils.pickColor()));
                }
                axisValues.add(new AxisValue(colIdx).setLabel(months[colIdx]));
                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }
            columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(4));

            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);
        }

        /**
         * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private void generateInitialLineData() {
            int numValues = 60;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            int step = 12;
            int idx = 0;
            for (int i = 0; i < numValues; ++i) {
                idx = i * 12;
                values.add(new PointValue(idx, 0));
                axisValues.add(new AxisValue(i).setLabel(idx+"s"));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            chartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            Viewport v = new Viewport(0, 50, 60, -50);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);
        }

        private void generateLineData(int color, float range) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation();

            // Modify data targets
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);
            for (PointValue value : line.getValues()) {
                // Change target only for Y value.
                value.setTarget(value.getX(), (float) Math.random() * range);
            }

            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(300);
        }

        private void generateLineDataNew(int colIdx, int color, float range) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation();

            // Modify data targets
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);

            Gson gson = new Gson();
            Type typeOfHashMap = new TypeToken<Map<Integer, ArrayList<Float>>>() { }.getType();
            Map<Integer,ArrayList<Float>> map = gson.fromJson(records, typeOfHashMap);

            Set set = map.entrySet();
            Iterator iterator = set.iterator();
            for (Map.Entry<Integer, ArrayList<Float>> mentry : map.entrySet()) {
                Integer idx = mentry.getKey();
                if (idx.equals(colIdx)) {
                    ArrayList<Float> subColData = mentry.getValue();
                    Float[] data = subColData.toArray(new Float[subColData.size()]);
                    int size = 0;
                    if (line.getValues().size() > (subColData.size()/12)) {
                        size = subColData.size() / 12;
                    } else {
                        size = line.getValues().size();
                    }
                    int newIdx = 0;
                    for(int i = 0; i < size; i++) {
                       newIdx = i * 12;
                       PointValue value = line.getValues().get(i);
                       value.setTarget(value.getX(), data[newIdx]);
                    }
                    break;
                }
            }
            // Start new data animation with 300ms duration;
            chartTop.startDataAnimation(300);
        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                //generateLineData(value.getColor(), 180);
                generateLineDataNew(columnIndex, value.getColor(), 180);
            }

            @Override
            public void onValueDeselected() {
                generateLineData(ChartUtils.COLOR_GREEN, 0);
            }
        }
    }
}
