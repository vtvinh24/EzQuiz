package dev.vtvinh24.ezquiz.data.repo;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.vtvinh24.ezquiz.network.api.model.AuthResponse;

public class UserRepository {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_PREMIUM = "is_premium";
    private static final String KEY_PREMIUM_EXPIRY = "premium_expiry";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_SYNC = "last_sync";

    private SharedPreferences sharedPreferences;
    private MutableLiveData<AuthResponse.UserData> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();

    public UserRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCurrentUser();
    }

    public void saveUserData(String token, AuthResponse.UserData userData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userData.getId());
        editor.putString(KEY_USER_NAME, userData.getName());
        editor.putString(KEY_USER_EMAIL, userData.getEmail());
        editor.putBoolean(KEY_IS_PREMIUM, userData.isPremium());
        editor.putString(KEY_PREMIUM_EXPIRY, userData.getPremiumExpiryDate());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        currentUser.postValue(userData);
        isLoggedIn.postValue(true);
    }

    public void updateUserData(AuthResponse.UserData userData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userData.getName());
        editor.putString(KEY_USER_EMAIL, userData.getEmail());
        editor.putBoolean(KEY_IS_PREMIUM, userData.isPremium());
        editor.putString(KEY_PREMIUM_EXPIRY, userData.getPremiumExpiryDate());
        editor.apply();

        currentUser.postValue(userData);
    }

    public String getJwtToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getAuthHeader() {
        String token = getJwtToken();
        return token != null ? "Bearer " + token : null;
    }

    public String getCurrentUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && getJwtToken() != null;
    }

    public LiveData<AuthResponse.UserData> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        currentUser.postValue(null);
        isLoggedIn.postValue(false);
    }

    public void setLastSyncTime(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_SYNC, timestamp);
        editor.apply();
    }

    public long getLastSyncTime() {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0);
    }

    private void loadCurrentUser() {
        if (isUserLoggedIn()) {
            AuthResponse.UserData userData = new AuthResponse.UserData();
            userData.setId(sharedPreferences.getString(KEY_USER_ID, ""));
            userData.setName(sharedPreferences.getString(KEY_USER_NAME, ""));
            userData.setEmail(sharedPreferences.getString(KEY_USER_EMAIL, ""));
            userData.setPremium(sharedPreferences.getBoolean(KEY_IS_PREMIUM, false));
            userData.setPremiumExpiryDate(sharedPreferences.getString(KEY_PREMIUM_EXPIRY, null));

            currentUser.postValue(userData);
            isLoggedIn.postValue(true);
        } else {
            currentUser.postValue(null);
            isLoggedIn.postValue(false);
        }
    }
}
