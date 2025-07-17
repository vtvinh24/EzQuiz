package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizModels {

    public static class QuizQuestion {
        @SerializedName("question")
        private String question;

        @SerializedName("answers")
        private List<String> answers;

        @SerializedName("correctAnswerIndices")
        private List<Integer> correctAnswerIndices;

        @SerializedName("type")
        private String type; // "SINGLE_CHOICE" or "MULTIPLE_CHOICE"

        // Getters and setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public List<String> getAnswers() { return answers; }
        public void setAnswers(List<String> answers) { this.answers = answers; }

        public List<Integer> getCorrectAnswerIndices() { return correctAnswerIndices; }
        public void setCorrectAnswerIndices(List<Integer> correctAnswerIndices) {
            this.correctAnswerIndices = correctAnswerIndices;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class ServerQuiz {
        @SerializedName("_id")
        private String id;

        @SerializedName("userId")
        private String userId;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("quizzes")
        private List<QuizQuestion> quizzes;

        @SerializedName("isPublic")
        private boolean isPublic;

        @SerializedName("tags")
        private List<String> tags;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<QuizQuestion> getQuizzes() { return quizzes; }
        public void setQuizzes(List<QuizQuestion> quizzes) { this.quizzes = quizzes; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class QuizListResponse {
        @SerializedName("quizzes")
        private List<ServerQuiz> quizzes;

        @SerializedName("pagination")
        private Pagination pagination;

        public static class Pagination {
            @SerializedName("page")
            private int page;

            @SerializedName("limit")
            private int limit;

            @SerializedName("total")
            private int total;

            @SerializedName("pages")
            private int pages;

            // Getters and setters
            public int getPage() { return page; }
            public void setPage(int page) { this.page = page; }

            public int getLimit() { return limit; }
            public void setLimit(int limit) { this.limit = limit; }

            public int getTotal() { return total; }
            public void setTotal(int total) { this.total = total; }

            public int getPages() { return pages; }
            public void setPages(int pages) { this.pages = pages; }
        }

        // Getters and setters
        public List<ServerQuiz> getQuizzes() { return quizzes; }
        public void setQuizzes(List<ServerQuiz> quizzes) { this.quizzes = quizzes; }

        public Pagination getPagination() { return pagination; }
        public void setPagination(Pagination pagination) { this.pagination = pagination; }
    }

    public static class QuizDetailResponse {
        @SerializedName("quiz")
        private ServerQuiz quiz;

        public ServerQuiz getQuiz() { return quiz; }
        public void setQuiz(ServerQuiz quiz) { this.quiz = quiz; }
    }

    public static class SaveQuizRequest {
        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("quizzes")
        private List<QuizQuestion> quizzes;

        @SerializedName("isPublic")
        private boolean isPublic;

        @SerializedName("tags")
        private List<String> tags;

        public SaveQuizRequest(String title, String description, List<QuizQuestion> quizzes,
                             boolean isPublic, List<String> tags) {
            this.title = title;
            this.description = description;
            this.quizzes = quizzes;
            this.isPublic = isPublic;
            this.tags = tags;
        }

        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public List<QuizQuestion> getQuizzes() { return quizzes; }
        public boolean isPublic() { return isPublic; }
        public List<String> getTags() { return tags; }
    }

    public static class QuizSaveResponse {
        @SerializedName("message")
        private String message;

        @SerializedName("quiz")
        private QuizInfo quiz;

        public static class QuizInfo {
            @SerializedName("id")
            private String id;

            @SerializedName("title")
            private String title;

            @SerializedName("description")
            private String description;

            @SerializedName("questionsCount")
            private int questionsCount;

            // Getters and setters
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }

            public int getQuestionsCount() { return questionsCount; }
            public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public QuizInfo getQuiz() { return quiz; }
        public void setQuiz(QuizInfo quiz) { this.quiz = quiz; }
    }

    public static class UpdateQuizRequest extends SaveQuizRequest {
        public UpdateQuizRequest(String title, String description, List<QuizQuestion> quizzes,
                               boolean isPublic, List<String> tags) {
            super(title, description, quizzes, isPublic, tags);
        }
    }

    public static class QuizUpdateResponse extends QuizSaveResponse {}

    public static class QuizDeleteResponse {
        @SerializedName("message")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
