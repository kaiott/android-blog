package com.example.blogrestframework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);
    }

    public void login(View view) {
        authenticate(usernameText.getText().toString(), passwordText.getText().toString());
    }

    public void useDefaultCredentials(View view) {
        usernameText.setText("TestUser");
        passwordText.setText("NqWwc2FUpyXNqzs");
    }

    private void authenticate(String username, String password) {
        String URL = "http://www.angri.li/api/token/";
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
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
                            String refreshToken = response.getString("refresh");
                            String accessToken = response.getString("access");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("refreshToken", refreshToken);
                            intent.putExtra("accessToken", accessToken);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            makeNotSuccessfulToast();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                        makeNotSuccessfulToast();
                    }
                }
        );
        requestQueue.add(objectRequest);
    }

    public void makeNotSuccessfulToast() {
        Toast.makeText(this, "Login did not succeed, try again", Toast.LENGTH_SHORT).show();
    }
}
