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
        // 👉 로그인 버튼 클릭 시
        btnLogin.setOnClickListener {
            val id = edtId.text.toString().trim()
            val pw = edtPw.text.toString()

            // 아이디나 비밀번호가 비어있는지 확인
            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DB에서 사용자 정보 확인
            if (dbHelper.checkUser(id, pw)) {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                // 👉 메인화면으로 이동
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // 현재 로그인 화면 종료 (뒤로가기 방지)
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 👉 회원가입 텍스트 클릭 시 회원가입 화면으로 이동
        tvRegister.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
    }
}