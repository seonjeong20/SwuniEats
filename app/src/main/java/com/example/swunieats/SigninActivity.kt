package com.example.swunieats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SigninActivity : AppCompatActivity() {

    // EditText와 Button들을 전역 변수로 선언 → onCreate에서 findViewById로 연결 예정
    lateinit var edtName: EditText
    lateinit var edtId: EditText
    lateinit var edtPw: EditText
    lateinit var edtPwConfirm: EditText
    lateinit var btnSignup: Button
    lateinit var btnCancel: Button
    lateinit var btnCheckId: Button
    lateinit var dbHelper: DBHelper

    // 아이디 중복확인을 했는지 기억하는 변수 → 중복 확인 없이 회원가입하는 것을 방지하기 위해 사용
    var isIdChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)  // activity_signin.xml 레이아웃 사용

        // XML에서 정의한 EditText와 Button을 코드와 연결
        edtName = findViewById(R.id.nameText)
        edtId = findViewById(R.id.idText)
        edtPw = findViewById(R.id.passwordText)
        edtPwConfirm = findViewById(R.id.passwordCheckText)
        btnSignup = findViewById(R.id.signinButton)
        btnCancel = findViewById(R.id.cancelButton)
        btnCheckId = findViewById(R.id.idCheckButton)

        // DBHelper 초기화 → SQLite DB에 접근할 수 있게 됨
        dbHelper = DBHelper(this)

        // 👉 아이디 중복 확인 버튼 클릭 시
        btnCheckId.setOnClickListener {
            val inputId = edtId.text.toString().trim()

            // 아이디 입력 안 한 경우는 확인할 수 없으므로 경고
            if (inputId.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DB에 같은 아이디가 이미 존재하면 중복 → 사용할 수 없음
            if (dbHelper.isIdDuplicate(inputId)) {
                Toast.makeText(this, "이미 사용 중인 아이디입니다", Toast.LENGTH_SHORT).show()
                isIdChecked = false  // 중복 상태로 저장
            } else {
                Toast.makeText(this, "사용 가능한 아이디입니다", Toast.LENGTH_SHORT).show()
                isIdChecked = true  // 중복 확인 완료로 저장
            }
        }

        // 👉 회원가입 버튼 클릭 시
        btnSignup.setOnClickListener {
            val name = edtName.text.toString().trim()
            val id = edtId.text.toString().trim()
            val pw = edtPw.text.toString()
            val pwConfirm = edtPwConfirm.text.toString()

            // 모든 필드가 비어있지 않은지 확인 → 필수 입력 항목 체크
            if (name.isEmpty() || id.isEmpty() || pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호와 확인 입력이 일치하는지 확인
            if (pw != pwConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 확인을 하지 않고 가입을 시도하면 막음 → 사용자 실수 방지
            if (!isIdChecked) {
                Toast.makeText(this, "아이디 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DB에 사용자 정보 저장 → insertUser는 성공 여부를 반환함
            if (dbHelper.insertUser(id, name, pw)) {
                Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()

                // 회원가입 성공 시 로그인 화면으로 이동 → startActivity 사용
                startActivity(Intent(this, LoginActivity::class.java))
                finish()  // 현재 화면 종료
            } else {
                // DB 삽입 실패 (예: 동일한 ID가 이미 존재) → insertUser 내부에서도 중복 방지되긴 함
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 👉 취소 버튼 클릭 시 현재 화면 닫기 → 이전 화면으로 돌아감
        btnCancel.setOnClickListener {
            finish()
        }
    }
}