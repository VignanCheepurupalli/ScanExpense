package com.example.androidfinalproject;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private ReceiptDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pieChart = findViewById(R.id.pieChart);
        dbHelper = new ReceiptDatabaseHelper(this);

        loadChartData();
    }

    private void loadChartData() {
        Cursor cursor = dbHelper.readAllReceipts();
        HashMap<String, Float> categoryTotals = new HashMap<>();

        while (cursor.moveToNext()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            float total = (float) cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + total);
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String category : categoryTotals.keySet()) {
            entries.add(new PieEntry(categoryTotals.get(category), category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
        dataSet.setColors(new int[]{
                R.color.blue, R.color.green, R.color.black, R.color.red,
                R.color.orange
        }, this);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(35f);

        Description description = new Description();
        description.setText("Category Spending");
        pieChart.setDescription(description);

        pieChart.invalidate(); // refresh
    }
}
