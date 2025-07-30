package com.example.swunieats

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    lateinit var edtId: EditText
    lateinit var edtPw: EditText
    lateinit var btnLogin: Button
    lateinit var tvRegister: TextView
    lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // 로그인 화면 레이아웃

        // XML ID와 연결
        edtId = findViewById(R.id.idText2)
        edtPw = findViewById(R.id.passwordText2)
        btnLogin = findViewById(R.id.loginButton2)
        tvRegister = findViewById(R.id.tvRegister)

        tvRegister.paintFlags = tvRegister.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        dbHelper = DBHelper(this)

        // 👉 로그인 버튼 클릭 시
        btnLogin.setOnClickListener {
            val id = edtId.text.toString().trim()
            val pw = edtPw.text.toString()

            // 아이디나 비밀번호가 비었는지 확인
            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (id == "tester" && pw == "test123") {
                Toast.makeText(this, "테스터 계정 로그인 성공!", Toast.LENGTH_SHORT).show()

                val db = dbHelper.writableDatabase
                val testerUserId = 1 // DB에서 테스터 계정의 userId가 1이라고 가정

                TestDataHelper.insertTestData(db, testerUserId)

                // 메인 액티비티로 이동
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (dbHelper.checkUser(id, pw)) {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                // 일반 사용자 로그인 처리
                // 메인 액티비티로 이동
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 👉 회원가입 버튼 클릭 시 회원가입 화면으로 이동
        tvRegister.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
    }
}