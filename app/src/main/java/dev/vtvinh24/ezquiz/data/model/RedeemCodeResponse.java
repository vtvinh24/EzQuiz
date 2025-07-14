package dev.vtvinh24.ezquiz.data.model;

public class RedeemCodeResponse {
    private String message;
    private Premium premium;

    public RedeemCodeResponse(String message, Premium premium) {
        this.message = message;
        this.premium = premium;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Premium getPremium() { return premium; }
    public void setPremium(Premium premium) { this.premium = premium; }

    public static class Premium {
        private boolean isPremium;
        private String premiumExpiryDate;
        private int daysAdded;
        private String codeType;

        public Premium(boolean isPremium, String premiumExpiryDate, int daysAdded, String codeType) {
            this.isPremium = isPremium;
            this.premiumExpiryDate = premiumExpiryDate;
            this.daysAdded = daysAdded;
            this.codeType = codeType;
        }

        public boolean isPremium() { return isPremium; }
        public void setPremium(boolean premium) { isPremium = premium; }

        public String getPremiumExpiryDate() { return premiumExpiryDate; }
        public void setPremiumExpiryDate(String premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

        public int getDaysAdded() { return daysAdded; }
        public void setDaysAdded(int daysAdded) { this.daysAdded = daysAdded; }

        public String getCodeType() { return codeType; }
        public void setCodeType(String codeType) { this.codeType = codeType; }
    }
}
