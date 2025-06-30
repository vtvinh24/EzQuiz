package dev.vtvinh24.ezquiz.util;

import android.media.Image;

import com.google.gson.Gson;

public class QRParser {
  // TODO: Implement QR code parsing logic
  public Gson getGsonFromQR(String qrCode) {
    // This method should parse the QR code string and return a Gson object
    // For now, we will just return a new Gson instance
    return new Gson();
  }

  public String getDataFromQR(Image image) {
    // TODO: This method should extract data from the QR code image
    // For now, we will just return an empty string
    return "";
  }
}
