package com.example.androidfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private List<Receipt> receiptList;
    private OnReceiptClickListener clickListener;

    public ReceiptAdapter(List<Receipt> receiptList, OnReceiptClickListener clickListener) {
        this.receiptList = receiptList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_row, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Receipt receipt = receiptList.get(position);
        holder.vendorText.setText(receipt.vendor);
        holder.dateText.setText(receipt.date);
        holder.totalText.setText("$" + String.format("%.2f", receipt.total));

        holder.itemView.setOnClickListener(v -> clickListener.onReceiptClick(receipt));

    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        TextView vendorText, dateText, totalText;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorText = itemView.findViewById(R.id.vendorText);
            dateText = itemView.findViewById(R.id.dateText);
            totalText = itemView.findViewById(R.id.totalText);
        }
    }

    public interface OnReceiptClickListener {
        void onReceiptClick(Receipt receipt);
    }
}
