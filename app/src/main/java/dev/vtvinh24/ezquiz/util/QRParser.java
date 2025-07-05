package dev.vtvinh24.ezquiz.util;

import android.graphics.ImageFormat;
import android.media.Image;

import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRParser {
  public Gson getGsonFromQR(String qrCode) {
    return new Gson();
  }

  public Task<String> getDataFromQR(Image image) {
    if (image == null || image.getFormat() != ImageFormat.YUV_420_888) {
      throw new IllegalArgumentException("Image must be in YUV_420_888 format");
    }
    InputImage inputImage = InputImage.fromMediaImage(image, 0);
    BarcodeScanner scanner = BarcodeScanning.getClient();
    return scanner.process(inputImage).continueWith(task -> {
      List<Barcode> barcodes = task.getResult();
      if (barcodes != null && !barcodes.isEmpty()) {
        return barcodes.get(0).getRawValue();
      }
      return "";
    });
  }
}
