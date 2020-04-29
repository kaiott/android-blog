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

    public static final String tokenSharedPreferencesName = "tokens";
    EditText usernameText, passwordText;
    String refreshToken, accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        refreshToken = getSharedPreferences(tokenSharedPreferencesName, MODE_PRIVATE).getString("refreshToken", null);
        accessToken = getSharedPreferences(tokenSharedPreferencesName, MODE_PRIVATE).getString("accessToken", null);
        if (refreshToken != null && accessToken != null) {
            goToMainActivity();
        }
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
        Map<String, String> params = new HashMap<>();
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
                            refreshToken = response.getString("refresh");
                            accessToken = response.getString("access");
                            getSharedPreferences(tokenSharedPreferencesName, MODE_PRIVATE)
                                    .edit()
                                    .putString("refreshToken", refreshToken)
                                    .putString("accessToken", accessToken)
                                    .apply();
                            goToMainActivity();
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

    public void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
