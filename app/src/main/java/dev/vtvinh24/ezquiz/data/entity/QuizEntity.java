package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import dev.vtvinh24.ezquiz.data.converter.IntegerListConverter;
import dev.vtvinh24.ezquiz.data.converter.StringListConverter;
import dev.vtvinh24.ezquiz.data.model.Quiz;

@Entity(tableName = "quiz")
public class QuizEntity {
  @PrimaryKey(autoGenerate = true)
  public long id;
  public String question;
  @TypeConverters(StringListConverter.class)
  public List<String> answers;
  @TypeConverters(IntegerListConverter.class)
  public List<Integer> correctAnswerIndices;
  public Quiz.Type type;
  public long quizSetId;
  public long createdAt = 0L;
  public long updatedAt = 0L;
  public boolean archived = false;
  public int difficulty = 0;
  public int order = 0;

  public boolean isFlashcard() {
      return type == Quiz.Type.FLASHCARD;
  }
}
