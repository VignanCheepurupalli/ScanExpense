package com.example.androidfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.pdf.PdfDocument;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import java.util.ArrayList;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class HistoryActivity extends AppCompatActivity {

    RecyclerView receiptRecyclerView;
    ReceiptDatabaseHelper dbHelper;
    ArrayList<Receipt> receiptList;
    ReceiptAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar tb = findViewById(R.id.historyToolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Expense History");


        receiptRecyclerView = findViewById(R.id.receiptRecyclerView);
        dbHelper = new ReceiptDatabaseHelper(this);
        receiptList = new ArrayList<>();

        loadReceipts();

        adapter = new ReceiptAdapter(receiptList, new ReceiptAdapter.OnReceiptClickListener() {
            @Override
            public void onReceiptClick(Receipt receipt) {
                Intent intent = new Intent(HistoryActivity.this, EditReceiptActivity.class);
                intent.putExtra("id", receipt.id);
                intent.putExtra("vendor", receipt.vendor);
                intent.putExtra("date", receipt.date);
                intent.putExtra("total", receipt.total);
                intent.putExtra("category", receipt.category);
                startActivity(intent);
            }
        });

        receiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiptRecyclerView.setAdapter(adapter);

        FloatingActionButton addManualButton = findViewById(R.id.addManualButton);
        addManualButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, EditReceiptActivity.class);
            intent.putExtra("manual", true); // signal that it's a manual add
            startActivity(intent);
        });

    }

    /*───────────────────────────────────────────────────────────────
  ExportPdfTask – writes ExpenseHistory_*.pdf into /exports
───────────────────────────────────────────────────────────────*/
    private class ExportPdfTask extends AsyncTask<Void, Void, File> {

        @Override protected void onPreExecute() {
            findViewById(R.id.historyProgress).setVisibility(View.VISIBLE);
        }

        @Override protected File doInBackground(Void... p) {

            // ①  Query DB for all receipts
            ReceiptDatabaseHelper db = new ReceiptDatabaseHelper(HistoryActivity.this);
            Cursor cur = db.readAllReceipts();

            // ②  Start a one-page PDF
            int w = 595, h = 842, y = 60, line = 20;
            PdfDocument doc = new PdfDocument();
            PdfDocument.Page page = doc.startPage(
                    new PdfDocument.PageInfo.Builder(w, h, 1).create());
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint(); paint.setTextSize(12);

            canvas.drawText("Expense History", 40, y, paint);
            y += line * 2;

            // ③  Stream DB rows straight onto the PDF
            while (cur.moveToNext()) {
                if (y > h - 60) {              // start new page if bottom reached
                    doc.finishPage(page);
                    page = doc.startPage(
                            new PdfDocument.PageInfo.Builder(w, h, 1).create());
                    canvas = page.getCanvas();
                    y = 60;
                }

                String date    = cur.getString(cur.getColumnIndexOrThrow("receipt_date"));
                String vendor  = cur.getString(cur.getColumnIndexOrThrow("vendor"));
                double total   = cur.getDouble(cur.getColumnIndexOrThrow("total"));

                String row = String.format("%s   %s   $%.2f", date, vendor, total);
                canvas.drawText(row, 40, y, paint);
                y += line;
            }
            cur.close();
            doc.finishPage(page);

            // ④  Save file to /exports
            File outDir = new File(getExternalFilesDir(null), "exports");
            if (!outDir.exists()) outDir.mkdirs();
            File pdf = new File(outDir,
                    "ExpenseHistory_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdf)) {
                doc.writeTo(fos);
            } catch (IOException e) { e.printStackTrace(); }
            doc.close();
            return pdf;
        }

        @Override protected void onPostExecute(File pdf) {
            findViewById(R.id.historyProgress).setVisibility(View.GONE);

            if (pdf == null || !pdf.exists()) {
                Toast.makeText(HistoryActivity.this,
                        "PDF export failed", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(
                    HistoryActivity.this,
                    HistoryActivity.this.getPackageName() + ".provider",
                    pdf);

            Intent view = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(view, "Open PDF with"));
        }
    }



    private void loadReceipts() {
        Cursor cursor = dbHelper.readAllReceipts();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String vendor = cursor.getString(cursor.getColumnIndexOrThrow("vendor"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("receipt_date"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                receiptList.add(new Receipt(id, vendor, date, total, category));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        receiptList.clear();
        loadReceipts();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_export_pdf) {
            new ExportPdfTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
