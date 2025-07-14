package dev.vtvinh24.ezquiz.data.model;

public class AuthResponse {
    private String token;
    private User user;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public static class User {
        private String id;
        private String email;
        private String name;
        private boolean isPremium;
        private String premiumExpiryDate;

        public User(String id, String email, String name, boolean isPremium, String premiumExpiryDate) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.isPremium = isPremium;
            this.premiumExpiryDate = premiumExpiryDate;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isPremium() { return isPremium; }
        public void setPremium(boolean premium) { isPremium = premium; }

        public String getPremiumExpiryDate() { return premiumExpiryDate; }
        public void setPremiumExpiryDate(String premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }
    }
}
