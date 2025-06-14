package dev.vtvinh24.ezquiz.data.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class StringListConverter {
  private static final Gson gson = new Gson();
  private static final Type type = new TypeToken<List<String>>() {
  }.getType();

  @TypeConverter
  public String fromList(List<String> list) {
    return list == null ? null : gson.toJson(list);
  }

  @TypeConverter
  public List<String> toList(String data) {
    return data == null ? null : gson.fromJson(data, type);
  }
}
