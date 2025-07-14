package dev.vtvinh24.ezquiz.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String email;
    private String name;
    private String token;
    private boolean isPremium;
    private String premiumExpiryDate;
    private long createdAt;
    private long lastLoginAt;
    private boolean isLoggedIn;

    public UserEntity(@NonNull String id, String email, String name, String token, boolean isPremium, String premiumExpiryDate, long createdAt, long lastLoginAt, boolean isLoggedIn) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.token = token;
        this.isPremium = isPremium;
        this.premiumExpiryDate = premiumExpiryDate;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.isLoggedIn = isLoggedIn;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public String getPremiumExpiryDate() { return premiumExpiryDate; }
    public void setPremiumExpiryDate(String premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
}
