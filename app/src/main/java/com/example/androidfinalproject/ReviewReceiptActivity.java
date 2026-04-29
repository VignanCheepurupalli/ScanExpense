package com.example.androidfinalproject;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReviewReceiptActivity extends AppCompatActivity {

    private EditText editVendor, editDate, editTotal;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_receipt);

        editVendor = findViewById(R.id.editVendor);
        editDate = findViewById(R.id.editDate);
        editTotal = findViewById(R.id.editTotal);
        categorySpinner = findViewById(R.id.categorySpinner);
        Button saveButton = findViewById(R.id.saveButton);

        // Setup category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.receipt_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Get data from intent
        String vendor = getIntent().getStringExtra("vendor");
        String date = getIntent().getStringExtra("date");
        String total = getIntent().getStringExtra("total");

        if (vendor == null || vendor.isEmpty()) {
            String uriStr = getIntent().getStringExtra("image_uri"); // key sent by CameraActivity
            if (uriStr != null) {
                Uri imgUri = Uri.parse(uriStr);

                OcrProcessor.extractTextFromImage(this, imgUri,
                        new OcrProcessor.OcrCallback() {
                            @Override public void onTextExtracted(String result) {
                                String v = ReceiptParser.extractVendor(result);
                                String d = ReceiptParser.extractDate(result);
                                String t = ReceiptParser.extractTotal(result);

                                editVendor.setText(v);
                                editDate.setText(d);
                                editTotal.setText(t);
                            }
                            @Override public void onError(Exception e) {
                                Toast.makeText(ReviewReceiptActivity.this,
                                        "OCR failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

        editVendor.setText(vendor);
        editDate.setText(date);
        editTotal.setText(total);

        saveButton.setOnClickListener(view -> {
            String v = editVendor.getText().toString().trim();
            String d = editDate.getText().toString().trim();
            String t = editTotal.getText().toString().trim();
            String c = categorySpinner.getSelectedItem().toString();

            if (v.isEmpty() || d.isEmpty() || t.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double totalValue = Double.parseDouble(t);

                ReceiptDatabaseHelper dbHelper = new ReceiptDatabaseHelper(this);
                dbHelper.insertReceipt(v, d, totalValue, c);

                Toast.makeText(this, "Receipt saved", Toast.LENGTH_SHORT).show();
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid total amount", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
