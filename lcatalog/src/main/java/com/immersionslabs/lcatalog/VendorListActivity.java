package com.immersionslabs.lcatalog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.immersionslabs.lcatalog.Utils.EnvConstants;
import com.immersionslabs.lcatalog.adapters.VendorListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class VendorListActivity extends AppCompatActivity {
    private static final String REGISTER_URL = EnvConstants.APP_BASE_URL + "/vendors";
    private static final String TAG = "VendorListActivity";
    RecyclerView recycler;
    GridLayoutManager VendorListManager;
    private ArrayList<String> vendor_ids;
    private ArrayList<String> vendor_names;
    private ArrayList<String> vendor_logos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_list);

        recycler = findViewById(R.id.vendor_list_recycler);
        recycler.setHasFixedSize(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        Toolbar toolbar = findViewById(R.id.toolbar_vendor_list);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        vendor_ids = new ArrayList<>();
        vendor_names = new ArrayList<>();
        vendor_logos = new ArrayList<>();

        CommonGetData();
    }

    private void CommonGetData() {
        Log.e(TAG, "CommonGetData: " + REGISTER_URL);
        final JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, REGISTER_URL, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: response" + response);

                try {
                    JSONArray resp = response.getJSONArray("data");
                    GetData(resp);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VendorListActivity.this, "Internal Error", Toast.LENGTH_SHORT).show();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject request = new JSONObject(res);
                        Log.e(TAG, "request--" + request);
                    } catch (UnsupportedEncodingException | JSONException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    }
                }
            }

        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void GetData(JSONArray resp) {

        for (int i = 0; i < resp.length(); i++) {
            JSONObject obj = null;

            try {
                obj = resp.getJSONObject(i);

                vendor_ids.add(obj.getString("id"));
                vendor_names.add(obj.getString("name"));
                vendor_logos.add(obj.getString("logo"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e(TAG, "GetData: ids" + vendor_ids);
        Log.e(TAG, "GetData: names" + vendor_names);
        Log.e(TAG, "GetData: logos" + vendor_logos);

        VendorListManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(VendorListManager);
        VendorListAdapter adapter = new VendorListAdapter(this, vendor_ids, vendor_names, vendor_logos);
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
