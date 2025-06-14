package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_set")
public class QuizSetEntity {
  @PrimaryKey(autoGenerate = true)
  public long id;
  public long collectionId;
  public String name;
  public String description;
  public long createdAt = 0L;
  public long updatedAt = 0L;
  public boolean archived = false;
  public int difficulty = 0;
}
