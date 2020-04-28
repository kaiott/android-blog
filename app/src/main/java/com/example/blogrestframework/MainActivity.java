package com.example.blogrestframework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    HashMap<Integer, Post> posts;
    HashMap<Integer, Profile> profiles;

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
        downloadPosts(15);
        downloadProfiles(-1);

        /*if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {}, PERMISSION_REQUEST_CODE);
        }*/
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
                    }
                }
        );
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
                    }
                }
        );
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
        for (Post post : result.values()) {
            Log.i("Main", "parseJson: Post: " + post.id + " \r\n" + post.title +
                    " \r\n" + post.content + " \r\n" + post.datePosted + " \r\n" + post.authorId + " \r\n" );
        }
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
        for (Profile profile : result.values()) {
            Log.i("Main", "parseJson: Profile: " + profile.id + " \r\n" + profile.userId +
                    " \r\n" + profile.username + " \r\n" + profile.imageUrl + " \r\n" );
        }
        return result;
    }

}
