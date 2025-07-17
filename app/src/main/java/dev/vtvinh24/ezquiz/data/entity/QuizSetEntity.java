package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_set")
public class QuizSetEntity {
  @PrimaryKey(autoGenerate = true)
  public long id;

  // Legacy field for backward compatibility
  public long collectionId;

  public String name;
  public String description;
  public long createdAt = 0L;
  public long updatedAt = 0L;
  public boolean archived = false;
  public int difficulty = 0;

  // New fields for user-specific and server sync
  public String userId;
  public String serverId;
  public boolean isFromServer = false;
  public long lastSyncTime = 0L;
  public boolean needsUpload = false;
  public int questionCount = 0;
}
