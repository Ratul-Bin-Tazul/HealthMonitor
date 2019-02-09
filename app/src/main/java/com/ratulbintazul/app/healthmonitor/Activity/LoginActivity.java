package com.ratulbintazul.app.healthmonitor.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ratulbintazul.app.healthmonitor.ApiClient.ApiClient;
import com.ratulbintazul.app.healthmonitor.ApiInterface.ApiInterface;
import com.ratulbintazul.app.healthmonitor.DataModel.LoginResponse;
import com.ratulbintazul.app.healthmonitor.DataModel.UserData;
import com.ratulbintazul.app.healthmonitor.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText email,pass;

    Button loginBt;

    TextView signup;

    ProgressDialog progressDialog;

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.loginEmail);
        pass = findViewById(R.id.loginPassword);
        loginBt = findViewById(R.id.loginButton);
        signup = findViewById(R.id.loginSignup);

        progressDialog = new ProgressDialog(this);

        SharedPreferences userPref = getSharedPreferences(
                getString(R.string.user_data_pref), Context.MODE_PRIVATE);

        String userString = userPref.getString(getString(R.string.user_data), "");

        if (userPref!=null && !userString.equals("")) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Signing in...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                login(email.getText().toString(),pass.getText().toString());
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });

    }

    private void login(String email, String pass) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<LoginResponse> venues = apiService.loginUser(email,pass);
        venues.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {


                LoginResponse loginResponse = response.body();

                Log.e("user",gson.toJson(loginResponse));
                getUserData(loginResponse.getUser(),loginResponse.getLoginString());

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                Toast.makeText(LoginActivity.this," login "+t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }

        });

    }

    private void getUserData(String user, final String hash) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<UserData> venues = apiService.getUserData(user);
        venues.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                UserData userData = response.body();

                userData.setHash(hash);

                SharedPreferences sharedPref = getSharedPreferences(
                        getString(R.string.user_data_pref), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.user_data), gson.toJson(userData));
                editor.apply();

                startActivity(new Intent(LoginActivity.this,MainActivity.class));

                Log.e("user",gson.toJson(userData));

            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                Toast.makeText(LoginActivity.this,"getUserData" + t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }

        });

    }
}
