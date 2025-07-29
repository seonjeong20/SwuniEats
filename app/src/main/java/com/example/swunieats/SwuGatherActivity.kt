// SwuGatherActivity.kt (이전에 보내주신 최종본)
package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SwuGatherActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper

    private lateinit var etRoomName: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    // private lateinit var etDeliveryFee: EditText // 이 줄이 없어야 합니다.
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swu_gather)

        dbHelper = DBHelper(this)

        etRoomName = findViewById(R.id.idText3)
        etTime = findViewById(R.id.editTextText)
        etLocation = findViewById(R.id.editTextText2)
        // etDeliveryFee = findViewById(R.id.deliveryFeeEditText) // 이 줄이 없어야 합니다.
        btnCreate = findViewById(R.id.loginButton)

        btnCreate.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            val time = etTime.text.toString().trim()
            val location = etLocation.text.toString().trim()

            if (roomName.isEmpty() || time.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatroomId = dbHelper.insertChatroom(roomName, time, location) // <<== 여기 deliveryFee 인자가 없어야 합니다.

            if (newChatroomId != -1L) {
                Toast.makeText(this, "채팅방 개설 성공!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "채팅방 개설 실패.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}