package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_collection")
public class QuizCollectionEntity {
  @PrimaryKey(autoGenerate = true)
  public long id;
  public String name;
  public String description;
  public long createdAt = 0L;
  public long updatedAt = 0L;
  public boolean archived = false;
  public int difficulty = 0;
  public int order = 0;
}
