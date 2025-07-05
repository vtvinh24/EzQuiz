package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dev.vtvinh24.ezquiz.R;

public class FinishActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_finish);

    Button btnDone = findViewById(R.id.btn_finish_done);
    btnDone.setOnClickListener(v -> finish());
  }
}