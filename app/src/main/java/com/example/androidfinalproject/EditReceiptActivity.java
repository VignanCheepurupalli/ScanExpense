package com.example.androidfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class EditReceiptActivity extends AppCompatActivity {

    EditText editVendor, editDate, editTotal;
    Spinner categorySpinner;
    Button updateButton, deleteButton;

    int id;
    ReceiptDatabaseHelper dbHelper;
    boolean isManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);

        editVendor = findViewById(R.id.editVendor);
        editDate = findViewById(R.id.editDate);
        editTotal = findViewById(R.id.editTotal);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        categorySpinner = findViewById(R.id.categorySpinner);

        // 🎯 Step 1: Set up category dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.receipt_categories, // defined in strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        dbHelper = new ReceiptDatabaseHelper(this);

        Intent intent = getIntent();
        isManual = intent.getBooleanExtra("manual", false);

        if (isManual) {
            // Manual entry: start with empty fields
            deleteButton.setVisibility(View.GONE);
            updateButton.setText("Add Receipt");

            updateButton.setOnClickListener(v -> {
                String vendor = editVendor.getText().toString();
                String date = editDate.getText().toString();
                double total = Double.parseDouble(editTotal.getText().toString());
                String category = categorySpinner.getSelectedItem().toString();

                dbHelper.insertReceipt(vendor, date, total, category);
                finish();
            });
        } else {
            // Edit mode: populate with existing data
            id = intent.getIntExtra("id", -1);
            String vendor = intent.getStringExtra("vendor");
            String date = intent.getStringExtra("date");
            double total = intent.getDoubleExtra("total", 0);
            String category = intent.getStringExtra("category");

            editVendor.setText(vendor);
            editDate.setText(date);
            editTotal.setText(String.valueOf(total));

            // 🎯 Step 2: Pre-select category
            if (category != null) {
                int spinnerPosition = adapter.getPosition(category);
                categorySpinner.setSelection(spinnerPosition);
            }

            updateButton.setOnClickListener(v -> {
                String newVendor = editVendor.getText().toString();
                String newDate = editDate.getText().toString();
                double newTotal = Double.parseDouble(editTotal.getText().toString());
                String newCategory = categorySpinner.getSelectedItem().toString();

                dbHelper.updateReceipt(id, newVendor, newDate, newTotal, newCategory);
                finish();
            });

            deleteButton.setOnClickListener(v -> {
                dbHelper.deleteReceipt(id);
                finish();
            });
        }
    }
}
