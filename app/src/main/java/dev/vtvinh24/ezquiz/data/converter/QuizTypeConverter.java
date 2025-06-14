package dev.vtvinh24.ezquiz.data.converter;

import androidx.room.TypeConverter;

import dev.vtvinh24.ezquiz.data.model.Quiz;

public class QuizTypeConverter {
  @TypeConverter
  public String fromType(Quiz.Type type) {
    return type == null ? null : type.name();
  }

  @TypeConverter
  public Quiz.Type toType(String name) {
    return name == null ? null : Quiz.Type.valueOf(name);
  }
}
