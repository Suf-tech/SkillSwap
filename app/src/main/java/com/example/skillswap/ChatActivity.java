package com.example.skillswap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button btnSend;
    private Toolbar chatToolbar;

    private ChatAdapter adapter;
    private ArrayList<MessageModel> messageList;
    private DatabaseHelper db;

    private String currentUserEmail;
    private String chatPartnerEmail;
    private int requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = new DatabaseHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = sp.getString("user_email", "");

        chatPartnerEmail = getIntent().getStringExtra("chat_with_email");
        requestId = getIntent().getIntExtra("request_id", -1); // Get specific session
        boolean isCompleted = getIntent().getBooleanExtra("isCompleted", false);

        chatToolbar = findViewById(R.id.chatToolbar);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);

        setSupportActionBar(chatToolbar);
        if (getSupportActionBar() != null) {
            String partnerName = db.getUserName(chatPartnerEmail);
            getSupportActionBar().setTitle(partnerName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        chatToolbar.setNavigationOnClickListener(v -> finish());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        loadMessages();

        if (isCompleted) {
            messageInput.setEnabled(false);
            messageInput.setHint("This swap is completed. Chat is read-only.");
            btnSend.setEnabled(false);
            btnSend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            btnSend.setOnClickListener(v -> {
                String text = messageInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    // Insert using specific request ID
                    if (db.insertMessage(requestId, currentUserEmail, chatPartnerEmail, text)) {
                        messageInput.setText("");
                        loadMessages();
                    } else {
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void loadMessages() {
        messageList = new ArrayList<>();
        Cursor cursor = db.getChatHistory(requestId); // Query specific session

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // columns: id(0), request_id(1), sender(2), receiver(3), text(4)
                String sender = cursor.getString(2);
                String receiver = cursor.getString(3);
                String text = cursor.getString(4);
                messageList.add(new MessageModel(sender, receiver, text));
            }
            cursor.close();
        }

        adapter = new ChatAdapter(this, messageList, currentUserEmail);
        chatRecyclerView.setAdapter(adapter);

        if (messageList.size() > 0) {
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }
    }
}