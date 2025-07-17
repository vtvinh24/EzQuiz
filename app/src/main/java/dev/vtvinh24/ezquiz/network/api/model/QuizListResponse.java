package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizListResponse {
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
