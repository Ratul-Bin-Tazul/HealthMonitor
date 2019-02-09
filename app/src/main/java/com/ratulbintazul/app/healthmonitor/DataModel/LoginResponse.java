package com.ratulbintazul.app.healthmonitor.DataModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("login_string")
    @Expose
    private String loginString;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getLoginString() {
        return loginString;
    }

    public void setLoginString(String loginString) {
        this.loginString = loginString;
    }

}