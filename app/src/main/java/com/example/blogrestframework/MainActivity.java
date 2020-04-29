package com.example.blogrestframework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    // All posts, key is Post.id
    HashMap<Integer, Post> posts;
    // All profiles, key is Profile.owner.id
    HashMap<Integer, Profile> profiles;

    static final String TAG = "MainActivity";

    static String refreshToken;
    static String accessToken;

    private static final int PERMISSION_REQUEST_CODE = 1000;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profiles = new HashMap<>();
        posts = new HashMap<>();
        refreshToken = getSharedPreferences(LoginActivity.tokenSharedPreferencesName, MODE_PRIVATE).getString("refreshToken", null);
        accessToken = getSharedPreferences(LoginActivity.tokenSharedPreferencesName, MODE_PRIVATE).getString("accessToken", null);
        if (refreshToken != null && accessToken != null) {
            Log.i(TAG, "onCreate: we have tokens");
            Log.i(TAG, "onCreate: refreshToken = " + refreshToken);
            Log.i(TAG, "onCreate: accessToken = " + accessToken);
            downloadPosts(15);
            downloadProfiles(-1);
        }
        else {
            Log.e(TAG, "onCreate: there are no tokens");
            makeGoToLoginAlert().show();
        }
    }

    private void downloadProfiles(int maxCount) {
        String URL = "http://www.angri.li/api/profiles/";
        if (maxCount > 0) {
            URL += "?max_count=15";
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest objectRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("Rest Response", response.toString());
                        profiles.putAll(parseProfiles(response));
                        updateUI();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                        refreshAndRepeat();
                    }
                }
        )  {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        requestQueue.add(objectRequest);
    }

    private void downloadPosts(int maxCount) {
        String URL = "http://www.angri.li/api/posts/";
        if (maxCount > 0) {
            URL += "?max_count=15";
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest objectRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("Rest Response", response.toString());
                        posts.putAll(parsePosts(response));
                        updateUI();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                        refreshAndRepeat();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        requestQueue.add(objectRequest);
    }

    private HashMap<Integer, Post> parsePosts(JSONArray jsonArray) {
        HashMap<Integer, Post> result = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonElement = jsonArray.getJSONObject(i);
                int id = jsonElement.getInt("id");
                String url = jsonElement.getString("url");
                String title = jsonElement.getString("title");
                String content = jsonElement.getString("content");
                String date_posted = jsonElement.getString("date_posted");
                OffsetDateTime datePosted = OffsetDateTime.parse(date_posted);
                int authorId = jsonElement.getInt("author");
                result.put(id, new Post(id, url, title, content, datePosted, authorId));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Main", "parseJson: number of posts = " + result.size());
        return result;
    }

    private void updateUI() {
        Log.i("TAG", "updateUI: " + profiles.keySet());
        //Collections.reverse(result);
        PostTileAdapter myAdapter = new PostTileAdapter(this, posts.values(), profiles);
        RecyclerView recyclerView = findViewById(R.id.postRecycler);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private HashMap<Integer, Profile> parseProfiles(JSONArray jsonArray) {
        HashMap<Integer, Profile> result = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonElement = jsonArray.getJSONObject(i);
                int id = jsonElement.getInt("id");
                int userId = jsonElement.getInt("user");
                String username = jsonElement.getString("username");
                String imageUrl = jsonElement.getString("image_url");
                result.put(userId, new Profile(id, userId, username, imageUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Main", "parseJson: number of posts = " + result.size());
        return result;
    }

    protected AlertDialog makeGoToLoginAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("You are not logged in")
                .setMessage("To see the latest posts please go to the login page and log in with your credentials")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout(null);
                    }
                })
                .setCancelable(false);
        return builder.create();
    }

    public void logout(View view) {
        // Clearing tokens
        getSharedPreferences(LoginActivity.tokenSharedPreferencesName, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void refreshAndRepeat() {
        String URL = "http://www.angri.li/api/token/refresh/";
        Map<String, String> params = new HashMap<>();
        params.put("refresh", refreshToken);
        JSONObject jsonObj = new JSONObject(params);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Rest Response", response.toString());
                        try {
                            accessToken = response.getString("access");
                            Log.i(TAG, "onResponse: got new access token: " + accessToken);
                            getSharedPreferences(LoginActivity.tokenSharedPreferencesName, MODE_PRIVATE)
                                    .edit()
                                    .putString("accessToken", accessToken)
                                    .apply();
                            downloadPosts(15);
                            downloadProfiles(-1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: didn't get new access token, need to log in again");
                            makeGoToLoginAlert();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                        Log.e(TAG, "onResponse: didn't get new access token, need to log in again");
                        makeGoToLoginAlert();
                    }
                }
        );
        requestQueue.add(objectRequest);
    }

}
