package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var rv: RecyclerView
    private lateinit var btnGather: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DBHelper(this)
        rv = findViewById(R.id.chatroomRecyclerView)
        btnGather = findViewById(R.id.gatherButton)

        rv.layoutManager = LinearLayoutManager(this)
        val list = db.getAllChatrooms()
        rv.adapter = ChatroomAdapter(list) { cr ->
            val i = Intent(this, CalculatorActivity::class.java)
            i.putExtra("CHATROOM_ID", cr.id)
            startActivity(i)
        }

        btnGather.setOnClickListener {
            startActivity(Intent(this, SwuGatherActivity::class.java))
        }
    }
}
