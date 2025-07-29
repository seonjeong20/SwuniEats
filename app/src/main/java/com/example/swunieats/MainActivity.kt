package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Button을 사용하므로 import가 필요합니다.
import androidx.activity.enableEdgeToEdge // 필요 없으면 삭제 (선택 사항)
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat // 필요 없으면 삭제 (선택 사항)
import androidx.core.view.WindowInsetsCompat // 필요 없으면 삭제 (선택 사항)
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers // 코루틴 디스패처 import
import kotlinx.coroutines.launch // 코루틴 런치 함수 import
import kotlinx.coroutines.withContext // 코루틴 컨텍스트 전환 import
import kotlinx.coroutines.CoroutineScope // CoroutineScope import

// AppCompatActivity를 상속받고, CoroutineScope를 구현합니다.
class MainActivity : AppCompatActivity(), CoroutineScope {
    // CoroutineScope를 위한 Dispatchers.Main (UI 스레드) 설정
    override val coroutineContext = Dispatchers.Main

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatroomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // 필요 없으면 삭제
        setContentView(R.layout.activity_main)
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // } // 필요 없으면 삭제

        dbHelper = DBHelper(this)

        // 🚨 DB 작업을 코루틴으로 백그라운드에서 실행
        // launch 블록 안에서 백그라운드 스레드로 전환하여 DB 작업을 수행합니다.
        launch {
            val chatrooms = withContext(Dispatchers.IO) { // IO 디스패처로 백그라운드 스레드 전환
                dbHelper.getAllChatrooms() // DB 읽기 작업
            }
            // DB 작업 완료 후 다시 메인 스레드(UI 스레드)로 돌아와 UI 업데이트
            recyclerView = findViewById(R.id.chatroomRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity) // 'this' 대신 'this@MainActivity' 사용

            adapter = ChatroomAdapter(chatrooms) { chatroom ->
                val intent = Intent(this@MainActivity, CalculatorActivity::class.java) // 'this' 대신 'this@MainActivity' 사용
                intent.putExtra("CHATROOM_ID", chatroom.id)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }


        // "슈니 모으기" 버튼 → 채팅방 개설 화면으로 이동
        findViewById<Button>(R.id.gatherButton).setOnClickListener {
            val intent = Intent(this, SwuGatherActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // onResume에서도 DB 작업을 코루틴으로 백그라운드에서 실행
        launch {
            val updatedChatrooms = withContext(Dispatchers.IO) { // IO 디스패처로 백그라운드 스레드 전환
                dbHelper.getAllChatrooms() // DB 읽기 작업
            }
            // DB 작업 완료 후 다시 메인 스레드(UI 스레드)로 돌아와 UI 업데이트
            adapter = ChatroomAdapter(updatedChatrooms) { chatroom ->
                val intent = Intent(this@MainActivity, CalculatorActivity::class.java) // 'this' 대신 'this@MainActivity' 사용
                intent.putExtra("CHATROOM_ID", chatroom.id)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }
    }
}
