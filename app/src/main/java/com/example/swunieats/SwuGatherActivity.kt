package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SwuGatherActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var etName: EditText
    private lateinit var etTime: EditText
    private lateinit var etLoc: EditText
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swu_gather)

        db = DBHelper(this)
        etName = findViewById(R.id.idText3)
        etTime = findViewById(R.id.editTextText)
        etLoc = findViewById(R.id.editTextText2)
        btnCreate = findViewById(R.id.loginButton)

        btnCreate.setOnClickListener {
            val n = etName.text.toString().trim()
            val t = etTime.text.toString().trim()
            val l = etLoc.text.toString().trim()
            if (n.isEmpty() || t.isEmpty() || l.isEmpty()) return@setOnClickListener Toast.makeText(
                this,
                "모두 입력",
                Toast.LENGTH_SHORT
            ).show()
            if (db.insertChatroom(n, t, l) != -1L) {
                Toast.makeText(this, "생성 성공", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else Toast.makeText(this, "생성 실패", Toast.LENGTH_SHORT).show()
        }
    }
}