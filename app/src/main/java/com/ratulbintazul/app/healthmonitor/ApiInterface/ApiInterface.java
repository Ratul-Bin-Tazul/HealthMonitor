package com.ratulbintazul.app.healthmonitor.ApiInterface;

import com.google.gson.JsonObject;
import com.ratulbintazul.app.healthmonitor.DataModel.LoginResponse;
import com.ratulbintazul.app.healthmonitor.DataModel.UserData;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface ApiInterface {

    //Login
    @POST("login.php")
    @FormUrlEncoded
    Call<LoginResponse> loginUser(@Field("Email") String email, @Field("Password") String password);

    //get user data
    @POST("user_info.php")
    @FormUrlEncoded
    Call<UserData> getUserData(@Field("UserID") String UserID);

    //signup
    @POST("signup.php")
    @FormUrlEncoded
    Call<LoginResponse> signup(@Field("Name") String Name,@Field("Email") String Email,@Field("Password") String Password,@Field("Height") String Height,
                               @Field("Weight") String Weight,@Field("Age") String Age,@Field("Blood") String Blood);

    //sync data
    @POST("insert_steps.php")
    @FormUrlEncoded
    Call<ResponseBody> sync(@Field("ID") String ID,@Field("Steps") String Steps,@Field("Distance") String Distance,@Field("Hash") String Hash);

//    //Dashboard restaurant
//    //sent to catchfood, update offer/info
//    @Multipart
//    @POST("api?")
//    Call<SendToCatchfoodResponse> sendToCatchfood(@Part("action") RequestBody action, @Part("type") RequestBody type,
//                                                  @Part("update_type") RequestBody update_type, @Part("user_id") RequestBody user_id,
//                                                  @Part("subject") RequestBody subject, @Part("body") RequestBody body,
//                                                  @Part List<MultipartBody.Part> image
//    );



}