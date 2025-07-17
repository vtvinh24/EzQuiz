package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("user")
    private AuthResponse.UserData user;

    public AuthResponse.UserData getUser() { return user; }
    public void setUser(AuthResponse.UserData user) { this.user = user; }
}
