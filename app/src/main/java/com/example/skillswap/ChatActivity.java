package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button btnSend;
    private Toolbar chatToolbar;

    private ChatAdapter adapter;
    private ArrayList<MessageModel> messageList;

    private DatabaseReference mDatabase;
    private String currentUserId;
    private String chatPartnerId;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // --- SESSION SE UID LEIN (For Stability) ---
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Intent Data
        chatPartnerId = getIntent().getStringExtra("chat_with_id");
        requestId = getIntent().getStringExtra("request_id");
        String partnerName = getIntent().getStringExtra("chat_with_name");
        boolean isCompleted = getIntent().getBooleanExtra("isCompleted", false);

        // XML Bindings (IDs: chatToolbar, chatRecyclerView, messageInput, btnSend)
        chatToolbar = findViewById(R.id.chatToolbar);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);

        setSupportActionBar(chatToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(partnerName != null ? partnerName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        chatToolbar.setNavigationOnClickListener(v -> finish());

        // Layout Manager Setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(this, messageList, currentUserId);
        chatRecyclerView.setAdapter(adapter);

        if (requestId != null) {
            loadMessages();
        }

        if (isCompleted) {
            messageInput.setEnabled(false);
            messageInput.setHint("Chat is closed.");
            btnSend.setEnabled(false);
        } else {
            btnSend.setOnClickListener(v -> {
                String text = messageInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    sendMessage(text);
                }
            });
        }
    }

    private void sendMessage(String text) {
        if (requestId == null) return;

        HashMap<String, Object> msgMap = new HashMap<>();
        msgMap.put("sender", currentUserId);
        msgMap.put("receiver", chatPartnerId);
        msgMap.put("messageText", text); // Sync with MessageModel
        msgMap.put("timestamp", System.currentTimeMillis());

        mDatabase.child("Chats").child(requestId).push().setValue(msgMap)
                .addOnSuccessListener(aVoid -> messageInput.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        mDatabase.child("Chats").child(requestId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    MessageModel msg = data.getValue(MessageModel.class);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                if (messageList.size() > 0) {
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}