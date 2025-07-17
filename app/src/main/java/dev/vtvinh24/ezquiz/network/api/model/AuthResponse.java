package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private UserData user;

    public static class UserData {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("isPremium")
        private boolean isPremium;

        @SerializedName("premiumExpiryDate")
        private String premiumExpiryDate;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isPremium() { return isPremium; }
        public void setPremium(boolean premium) { isPremium = premium; }

        public String getPremiumExpiryDate() { return premiumExpiryDate; }
        public void setPremiumExpiryDate(String premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserData getUser() { return user; }
    public void setUser(UserData user) { this.user = user; }
}
