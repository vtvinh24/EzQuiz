package dev.vtvinh24.ezquiz.data.model;

public class QRData {
  private String dataType;  // "quizzes" or "flashcards"
  private String type;      // "request" or other future types
  private String url;       // URL to fetch the data from

  public QRData() {
    // Default constructor for Gson
  }

  public QRData(String dataType, String type, String url) {
    this.dataType = dataType;
    this.type = type;
    this.url = url;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isQuizData() {
    return "quizzes".equals(dataType);
  }

  public boolean isFlashcardData() {
    return "flashcards".equals(dataType);
  }

  public boolean isRequestType() {
    return "request".equals(type);
  }
}
