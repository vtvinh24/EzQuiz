package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class TokenValidationResponse {
    @SerializedName("valid")
    private boolean valid;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
