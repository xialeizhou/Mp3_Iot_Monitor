package com.unimelb.utils;

/**
 * Created by xialeizhou on 9/21/15.
 */

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;


import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class SerenaRequest extends Request<String> {

    private Response.Listener<String> listener;
    private Map<String, String> params;

    /**
     * @param url
     * @param params
     * @param reponseListener
     * @param errorListener
     */
    public SerenaRequest(String url, Map<String, String> params,
                         Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    /**
     * @param method
     * @param url
     * @param params
     * @param reponseListener
     * @param errorListener
     */
    public SerenaRequest(int method, String url, Map<String, String> params,
                         Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    /**
     * Getter for property 'params'.
     *
     * @return Value for property 'params'.
     */
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    @Override
    protected void deliverResponse(String response) {
    }
}