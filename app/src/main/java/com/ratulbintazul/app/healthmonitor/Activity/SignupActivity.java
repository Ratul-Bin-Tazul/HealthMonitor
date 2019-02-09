package com.ratulbintazul.app.healthmonitor.Activity;

import android.app.Dialog;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.ratulbintazul.app.healthmonitor.ApiClient.ApiClient;
import com.ratulbintazul.app.healthmonitor.ApiInterface.ApiInterface;
import com.ratulbintazul.app.healthmonitor.DataModel.LoginResponse;
import com.ratulbintazul.app.healthmonitor.DataModel.UserData;
import com.ratulbintazul.app.healthmonitor.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    EditText name,email,pass,height,weight,age,blood;

    Button signupBtn;
    private Gson gson = new Gson();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.signupName);
        email = findViewById(R.id.signupEmail);
        pass = findViewById(R.id.signupPassword);
        height = findViewById(R.id.signupHeight);
        weight = findViewById(R.id.signupWeight);
        age = findViewById(R.id.signupAge);
        blood = findViewById(R.id.signupBlood);
        signupBtn = findViewById(R.id.signupButton);

        progressDialog = new ProgressDialog(this);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Signing in...");
                progressDialog.setCancelable(false);
                progressDialog.show();


                signup(name.getText().toString(),email.getText().toString(),pass.getText().toString(),height.getText().toString()
                        ,weight.getText().toString(),age.getText().toString(),blood.getText().toString());
            }
        });
    }

    private void signup(String name, String email, String pass, String height, String weight, String age, String blood) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<LoginResponse> venues = apiService.signup(name,email,pass,height,weight,age,blood);
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

                Toast.makeText(SignupActivity.this," signup "+t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

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

                startActivity(new Intent(SignupActivity.this,MainActivity.class));

                Log.e("user",gson.toJson(userData));

            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                Toast.makeText(SignupActivity.this,"getUserData" + t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }

        });

    }

}
