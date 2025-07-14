package dev.vtvinh24.ezquiz.util;

public class UserLimits {
    // Free user limits
    public static final int FREE_MAX_QUIZ_QUESTIONS = 10;
    public static final int FREE_MAX_IMAGES_PER_SESSION = 3;
    public static final int FREE_MAX_QUIZ_SETS_PER_DAY = 5;
    public static final int FREE_MAX_COLLECTIONS = 3;

    // Premium user limits (much higher or unlimited)
    public static final int PREMIUM_MAX_QUIZ_QUESTIONS = 100;
    public static final int PREMIUM_MAX_IMAGES_PER_SESSION = 20;
    public static final int PREMIUM_MAX_QUIZ_SETS_PER_DAY = -1; // Unlimited
    public static final int PREMIUM_MAX_COLLECTIONS = -1; // Unlimited

    // Rate limiting
    public static final long FREE_MIN_REQUEST_INTERVAL_MS = 30000; // 30 seconds between requests
    public static final long PREMIUM_MIN_REQUEST_INTERVAL_MS = 5000; // 5 seconds between requests

    // Error messages
    public static final String ERROR_QUIZ_LIMIT_EXCEEDED = "Bạn đã đạt giới hạn số câu hỏi cho tài khoản miễn phí. Nâng cấp Premium để tạo thêm nhiều câu hỏi.";
    public static final String ERROR_IMAGE_LIMIT_EXCEEDED = "Bạn đã đạt giới hạn số ảnh cho tài khoản miễn phí. Nâng cấp Premium để sử dụng thêm ảnh.";
    public static final String ERROR_REQUEST_TOO_FREQUENT = "Vui lòng đợi %d giây trước khi tạo quiz tiếp theo.";
    public static final String ERROR_DAILY_LIMIT_EXCEEDED = "Bạn đã đạt giới hạn tạo quiz hàng ngày. Nâng cấp Premium để sử dụng không giới hạn.";
}
