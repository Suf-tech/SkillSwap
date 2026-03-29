package com.example.skillswap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class RequestAdapter extends BaseAdapter {
    Context context;
    ArrayList<RequestModel> list;
    String currentUser;
    DatabaseHelper db;

    public RequestAdapter(Context context, ArrayList<RequestModel> list, String currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
        this.db = new DatabaseHelper(context);
    }

    @Override
    public int getCount() { return list.size(); }
    @Override
    public Object getItem(int i) { return list.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.request_item, viewGroup, false);
        }

        RequestModel req = list.get(i);
        TextView type = view.findViewById(R.id.reqType);
        TextView userEmail = view.findViewById(R.id.reqUserEmail);
        TextView details = view.findViewById(R.id.reqDetails);
        TextView message = view.findViewById(R.id.reqMessage);
        TextView status = view.findViewById(R.id.reqStatus);
        LinearLayout actionLayout = view.findViewById(R.id.actionLayout);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnReject = view.findViewById(R.id.btnReject);

        status.setText(req.getStatus());
        if(req.getStatus().equals("Accepted")) status.setTextColor(Color.GREEN);
        else if(req.getStatus().equals("Rejected")) status.setTextColor(Color.RED);
        else status.setTextColor(Color.parseColor("#FF9800"));

        if (req.getSender().equalsIgnoreCase(currentUser)) {
            type.setText("OUTGOING");
            type.setTextColor(Color.parseColor("#4CAF50"));
            userEmail.setText("To: " + req.getReceiver());
            actionLayout.setVisibility(View.GONE);
        } else {
            type.setText("INCOMING");
            type.setTextColor(Color.parseColor("#2196F3"));
            userEmail.setText("From: " + req.getSender());
            actionLayout.setVisibility(req.getStatus().equals("Pending") ? View.VISIBLE : View.GONE);
        }

        details.setText("Wants: " + req.getRequired() + "\nOffers: " + req.getOffered());
        message.setText("\"" + req.getMsg() + "\"");

        btnAccept.setOnClickListener(v -> {
            if (db.updateRequestStatus(req.getId(), "Accepted")) {
                Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                ((RequestActivity)context).loadRequests();
            }
        });

        btnReject.setOnClickListener(v -> {
            if (db.updateRequestStatus(req.getId(), "Rejected")) {
                Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show();
                ((RequestActivity)context).loadRequests();
            }
        });

        return view;
    }
}