package dev.vtvinh24.ezquiz.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "quiz_sets")
public class QuizSet {
  @PrimaryKey(autoGenerate = true)
  public long id;

  public String name;
  public String description;
  public long createdAt;
  public long lastModified;
  public boolean archived;
  public int difficulty;

  // Thêm các trường mới cho user và server sync
  public String userId;          // ID của user sở hữu quiz set này
  public String serverId;        // ID của quiz trên server (null nếu chưa sync)
  public boolean isFromServer;   // true nếu quiz này được tải từ server
  public long lastSyncTime;      // Thời gian sync lần cuối
  public boolean needsUpload;    // true nếu quiz cần được upload lên server
  public int questionCount;      // Số lượng câu hỏi

  // Constructor mặc định cho Room
  public QuizSet() {
    this.createdAt = System.currentTimeMillis();
    this.lastModified = System.currentTimeMillis();
    this.archived = false;
    this.difficulty = 1;
    this.isFromServer = false;
    this.needsUpload = false;
    this.questionCount = 0;
  }

  // Constructor với các tham số cần thiết
  public QuizSet(String name, String description, String userId) {
    this();
    this.name = name;
    this.description = description;
    this.userId = userId;
  }

  // Constructor tương thích với code cũ
  public QuizSet(String name, String description, List<Quiz> quizzes,
                 long createdAt, long updatedAt, boolean archived, int difficulty) {
    this.name = name;
    this.description = description;
    this.createdAt = createdAt;
    this.lastModified = updatedAt;
    this.archived = archived;
    this.difficulty = difficulty;
    this.isFromServer = false;
    this.needsUpload = false;
    this.questionCount = quizzes != null ? quizzes.size() : 0;
  }

  // Getters
  public String getName() { return name; }
  public String getDescription() { return description; }
  public long getCreatedAt() { return createdAt; }
  public long getUpdatedAt() { return lastModified; }
  public boolean isArchived() { return archived; }
  public int getDifficulty() { return difficulty; }

  // Getters cho các trường mới
  public String getUserId() { return userId; }
  public String getServerId() { return serverId; }
  public boolean isFromServer() { return isFromServer; }
  public long getLastSyncTime() { return lastSyncTime; }
  public boolean needsUpload() { return needsUpload; }
  public int getQuestionCount() { return questionCount; }

  // Setters
  public void setName(String name) {
    this.name = name;
    this.lastModified = System.currentTimeMillis();
    if (!isFromServer) this.needsUpload = true;
  }

  public void setDescription(String description) {
    this.description = description;
    this.lastModified = System.currentTimeMillis();
    if (!isFromServer) this.needsUpload = true;
  }

  public void markAsUploaded(String serverId) {
    this.serverId = serverId;
    this.isFromServer = true;
    this.needsUpload = false;
    this.lastSyncTime = System.currentTimeMillis();
  }

  public void markAsModified() {
    this.lastModified = System.currentTimeMillis();
    if (this.serverId != null) {
      this.needsUpload = true;
    }
  }
}
