package com.example.androidfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    FloatingActionButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardScanner = findViewById(R.id.cardScanner);
        cardScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch your existing camera flow
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("mode", "SCAN");
                startActivity(intent);
            }
        });

        CardView cardOcr = findViewById(R.id.cardOcr);
        cardOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the camera + OCR processing
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("mode", "OCR");
                startActivity(intent);
            }
        });

        // Auto-Categorization stub
        CardView cardAutoCategory = findViewById(R.id.cardAutoCategory);
        cardAutoCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                                "Auto-Categorization coming soon!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Expense History
        CardView cardExpenseHistory = findViewById(R.id.cardExpenseHistory);
        cardExpenseHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Analytics Dashboard
        CardView cardAnalyticsDashboard = findViewById(R.id.cardAnalyticsDashboard);
        cardAnalyticsDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        // Spending Limit Alerts stub
        CardView cardSpendingLimitAlerts = findViewById(R.id.cardSpendingLimitAlerts);
        cardSpendingLimitAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                                "Spending Limit Alerts coming soon!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Calendar Entries stub
        CardView cardCalendarEntries = findViewById(R.id.cardCalendarEntries);
        cardCalendarEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                                "Calendar Entries coming soon!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Smart Location Logging stub
        CardView cardSmartLocationLogging = findViewById(R.id.cardSmartLocationLogging);
        cardSmartLocationLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                                "Smart Location Logging coming soon!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        captureButton = findViewById(R.id.captureButton);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_export) {
            exportReceiptsToCSV();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Log.d("MainActivity", "Received image URI: " + imageUri);
            Toast.makeText(this, "Processing receipt...", Toast.LENGTH_SHORT).show();

            OcrProcessor.extractTextFromImage(this, imageUri, new OcrProcessor.OcrCallback() {
                @Override
                public void onTextExtracted(String result) {
                    Log.d("OCR Result", result);

                    String vendor = ReceiptParser.extractVendor(result);
                    String date = ReceiptParser.extractDate(result);
                    String total = ReceiptParser.extractTotal(result);

                    //Log.d("Parsed Info", "Vendor: " + vendor);
                    //Log.d("Parsed Info", "Date: " + date);
                    //Log.d("Parsed Info", "Total: " + total);

                    //Toast.makeText(MainActivity.this, "Vendor: " + vendor + "\nDate: " + date + "\nTotal: $" + total, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MainActivity.this, ReviewReceiptActivity.class);
                    intent.putExtra("vendor", vendor);
                    intent.putExtra("date", date);
                    intent.putExtra("total", total);
                    startActivity(intent);

                }

                @Override
                public void onError(Exception e) {
                    Log.e("OCR Error", "Failed to extract text", e);
                    Toast.makeText(MainActivity.this, "Failed to process receipt.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void exportReceiptsToCSV() {
        ReceiptDatabaseHelper dbHelper = new ReceiptDatabaseHelper(this);
        Cursor cursor = dbHelper.readAllReceipts();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No receipts to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder data = new StringBuilder();
        data.append("ID,Vendor,Date,Total,Category\n");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String vendor = cursor.getString(cursor.getColumnIndexOrThrow("vendor"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("receipt_date"));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            data.append(id).append(",")
                    .append(vendor).append(",")
                    .append(date).append(",")
                    .append(total).append(",")
                    .append(category).append("\n");
        }

        try {
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) exportDir.mkdirs();

            File file = new File(exportDir, "receipts_export.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.close();

            Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Optional: share the file
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "My Receipts Export");
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share CSV"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

}
