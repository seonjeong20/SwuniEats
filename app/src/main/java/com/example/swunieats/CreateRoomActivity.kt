package com.example.swunieats

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CreateRoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val titleEditText = findViewById<EditText>(R.id.editTextTitle)
        val placeEditText = findViewById<EditText>(R.id.editTextPlace)
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val feeEditText = findViewById<EditText>(R.id.editTextFee)
        val createButton = findViewById<Button>(R.id.buttonCreate)

        createButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val place = placeEditText.text.toString()
            val time = timeEditText.text.toString()
            val fee = feeEditText.text.toString().toIntOrNull() ?: 0

            Toast.makeText(this, "방 생성 완료: $title", Toast.LENGTH_SHORT).show()
        }
    }
}