# Hướng Dẫn Phát Triển EzQuiz

Tài liệu này cung cấp hướng dẫn để phát triển ứng dụng Android EzQuiz, một ứng dụng học tập thông
qua câu hỏi trắc nghiệm nâng cao với các tính năng tạo, chỉnh sửa, nhập và học tập các bộ câu hỏi.

## Tổng Quan Dự Án

EzQuiz là một ứng dụng trắc nghiệm toàn diện cho phép người dùng:

- Tạo và chỉnh sửa bộ sưu tập câu hỏi
- Hỗ trợ nhiều loại câu hỏi (single choice, multiple choice, text input)
- Nhập câu hỏi từ nguồn bên ngoài như Quizlet
- Luyện tập với các tính năng học tập nâng cao

## Tính Năng Chính

### 1. Quản Lý Bộ Sưu Tập Câu Hỏi

- Tạo bộ sưu tập câu hỏi mới
- Chỉnh sửa bộ sưu tập hiện có
- Xóa bộ sưu tập
- Duyệt và tìm kiếm bộ sưu tập

### 2. Hỗ Trợ Các Loại Câu Hỏi

- Single choice
- Multiple choice
- Text input (tự luận)

### 3. Nhập Từ Nguồn Bên Ngoài

- Nhập bộ câu hỏi từ Quizlet
- Parse và chuyển đổi định dạng bên ngoài sang data model của ứng dụng

### 4. Chế Độ Học Nâng Cao

- **Retry Mode**: Các câu hỏi trả lời sai sẽ được lặp lại vào cuối phiên học
- **Shuffle Mode**: Thứ tự câu hỏi ngẫu nhiên để học tập hiệu quả hơn
- **Spaced Repetition**: Lên lịch thông minh cho các câu hỏi dựa trên hiệu suất

## Cấu Trúc Dự Án

Ứng dụng tuân theo kiến trúc MVVM và được tổ chức thành các thành phần chính sau:

### Data Layer

- **Model**: Core data entity đại diện cho các quiz và collection
- **Repository**: Data access interface và implementation
- **Data Sources**: Local (Room) và remote (Quizlet) data provider

### Domain Layer

- Business logic cho việc quản lí quiz, các phiên học, và importing

### UI Layer

- Activity và Fragment cung cấp user interface
- ViewModel xử lý UI-related data và logic

## Hướng Dẫn Triển Khai

### Step 1: Setup Database

Triển khai Room database để lưu trữ các quiz collection, các individual quiz, và user progress:

1. Hoàn thiện lớp `QuizDatabase.java` với các entity và DAO phù hợp
2. Triển khai các type converter cho các data type phức tạp
3. Thiết lập database migration strategy

### Step 2: Triển Khai Repository

Tạo các concrete implementation của các repository interface:

1. Triển khai `LocalQuizCollectionRepository.java`
2. Triển khai `LocalQuizRepository.java`
3. Thêm các method để search và filter các quiz

### Step 3: Tích Hợp Quizlet

Triển khai chức năng import sử dụng Retrofit và Jsoup:

1. Hoàn thiện `QuizletService.java` với các API endpoint
2. Triển khai HTML parsing để trích xuất dữ liệu Quizlet
3. Tạo các converter giữa Quizlet format và data model của ứng dụng

### Step 4: Logic Phiên Học

Triển khai quiz play mode với các tính năng học tập nâng cao:

1. Viết spaced repetition algorithm trong `PlaySessionManager.java`
2. Triển khai tính năng shuffle và retry
3. Tạo hệ thống quản lý tiến độ và hiệu suất

### Step 5: Triển Khai UI

UI cho tất cả các tính năng của ứng dụng:

1. Tạo các layout cho tất cả các fragment
2. Triển khai các ViewModel với LiveData cho các reactive UI update
3. Tạo các adapter cho các RecyclerView
4. Triển khai navigation giữa các screen

## Testing Strategy (optional)

1. **Unit Tests**: Test các repository implementation, quiz logic, và algorithm
2. **Integration Tests**: Test các database operation và retrofit service
3. **UI Tests**: Validate các user flow với Espresso

## Best Practices

1. Tuân theo các SOLID principle trong toàn bộ codebase
2. Sử dụng dependency injection cho các cleaner component relationship
3. Triển khai proper error handling và offline support
4. Thêm comprehensive logging để debugging
5. Tuân theo các material design guideline cho UI

## Dependencies

Ứng dụng sử dụng các key library sau:

- **AndroidX**: Core, AppCompat, Fragment
- **UI**: Material Components, ConstraintLayout, RecyclerView, ViewPager2
- **Architecture**: ViewModel, LiveData, Navigation
- **Database**: Room
- **Networking**: Retrofit, OkHttp, Jsoup
- **JSON Processing**: Gson
- **Image Loading**: Glide
- **Background Processing**: WorkManager

## Next Steps

1. Bắt đầu với việc triển khai data layer
2. Chuyển sang triển khai business logic
3. Tạo các UI component và connect chúng với các ViewModel
4. Triển khai import functionality
5. Thêm play mode với các learning feature
6. Polish UI và thêm các animation
7. Triển khai comprehensive error handling
8. Thêm analytics để track user learning progress
