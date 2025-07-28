package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swunieats.CreateRoomActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatroomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        val chatrooms = dbHelper.getAllChatrooms()
        recyclerView = findViewById(R.id.chatroomRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatroomAdapter(chatrooms) { chatroom ->
            val intent = Intent(this, SwuGatherActivity::class.java)
            // 필요 시 chatroom 정보 전달 가능
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // 아래 버튼: "슈니 모으기" → 채팅방 새로 생성하는 화면으로 이동
        findViewById<Button>(R.id.gatherButton).setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }
    }
}
