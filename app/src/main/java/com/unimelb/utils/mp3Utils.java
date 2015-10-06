package com.unimelb.utils;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.base.CharMatcher;

import org.json.JSONObject;

import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xialeizhou on 9/20/15.
 */
public class mp3Utils {
    private static BigInteger bigval = null;
    private static int intval;
    private static final int ONE_DAY = 24 * 60 * 60 * 1000;
    private RequestQueue queue;
    BigInteger count = BigInteger.ONE;

    /**
     * @param str
     * @return
     */
    public static BigInteger string2bigint(String str) {
        bigval = new BigInteger(str);
        return bigval;
    }

    /**
     * @param str
     * @return
     */
    public static int string2int(String str) {
        intval = Integer.parseInt(str);
        return intval;
    }

    public static String getCurrTime() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(new Date());
    }

    public static String getDateTimeRelCurrTime(int param) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, param);
        return df.format(c.getTime());
    }

    public BigInteger getCount() {
        return count;
    }

    /**
     * @param context
     * @param url
     */
    public List<String> doHttpQuery(final Context context, String url, Map<String, String> params) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        final List<String> respList = new ArrayList<String>();
        RequestQueue queue = Volley.newRequestQueue(context);
        String fixedUrl = fixQueryUrl(url, params);
        //fixedUrl = "http://49.213.15.196/mp3-iot-service/serena/temperature20150921031706/20150921031707";
//        fixedUrl = "http://49.213.15.196/mp3-iot-service/serena/temperature/get/20150921031706/20150921031707";
        StringRequest stringReq = new StringRequest(Request.Method.POST, fixedUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
						//debugMsg(context, "response str:" + response.toString());
                        respList.add(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                debugMsg(context, "failed to do volley request.");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringReq);
        return respList;
    }

    /**
     * @param context
     * @param url
     */
    public void doHttpUpdate(final Context context, String url,  Map<String, String> params) {
        try {
            if (queue == null) {
                queue = Volley.newRequestQueue(context);
            }
            String fixedUrl = fixUpdateUrl(url, params);
            String idx = params.get("idx");
            StringRequest stringReq = new StringRequest(Request.Method.POST, fixedUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            debugMsg(context, "update state:" + response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //debugMsg(context, "failed to do volley request." + error.getMessage());
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringReq);
            count = count.add(BigInteger.ONE);
            debugMsg(context, "task queue [" + count + "]: added");
        } catch (Exception e) {
            debugMsg(context, "failed to do voley request.");
            return;
        }
    }

    /**
     * Display debug message on app's screen
     * @param context
     * @param str
     */
    public static void debugMsg(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    /**
     * @param url
     * @param params
     * @return
     */
    protected static String fixQueryUrl(String url, Map<String, String> params) {
        if (params.size() < 2) return url;
        return url + "/" + params.get("stime") + "/" + params.get("etime");
    }

    /**
     * @param url
     * @param params
     * @return
     */
    protected  static String fixUpdateUrl(String url, Map<String, String> params) {
       if (params.size() < 2) return null;
        return url + "/" + params.get("submit_time") + "/" + params.get("value");
    }

    public static String cleanNonPrintableChars(String s) {
        String printable = CharMatcher.INVISIBLE.removeFrom(s);
        String clean = CharMatcher.ASCII.retainFrom(printable).replaceAll("[><=]", "");;
        return clean;
    }

    public static void main(String [] args) {
        mp3Utils util = new mp3Utils();
        System.out.println(getCurrTime());
        System.out.println(getDateTimeRelCurrTime(-1));
        System.out.println(getDateTimeRelCurrTime(-2));
        System.out.println(getDateTimeRelCurrTime(1));
        System.out.println(getDateTimeRelCurrTime(3));
        // query acc
    }

}
