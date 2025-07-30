package com.example.swunieats

import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var container: LinearLayout
    private var userCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        db = DBHelper(this)
        container = findViewById(R.id.userBlocksContainer)

        findViewById<Button>(R.id.btnAddUser).setOnClickListener { addUserBlock() }
        findViewById<Button>(R.id.btnCalculate).setOnClickListener { calculate() }
        addUserBlock()
    }

    private fun addUserBlock() {
        userCount++
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0,16,0,16)
        }
        val tv = TextView(this).apply { text = "User $userCount" }
        block.addView(tv)
        val menuContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        block.addView(menuContainer)
        val btnMenu = Button(this).apply {
            text = "메뉴 추가"
            setOnClickListener { addMenuField(menuContainer) }
        }
        block.addView(btnMenu)
        container.addView(block)
    }

    private fun addMenuField(c: LinearLayout) {
        val etName = EditText(this).apply { hint = "메뉴 이름" }
        val etPrice = EditText(this).apply {
            hint = "가격"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        c.addView(etName)
        c.addView(etPrice)
    }

    private fun calculate() {
        var total = 0.0
        val users = container.childCount

        // 1) for-루프로 사용자 블록 순회
        for (i in 0 until users) {
            val block = container.getChildAt(i) as ViewGroup
            val menuCont = block.getChildAt(1) as LinearLayout
            val fields = menuCont.childCount

            // 2) 0부터 fields-1까지, 2칸씩 건너뛰며 가격 합산
            for (j in 0 until fields step 2) {
                val priceEt = menuCont.getChildAt(j + 1) as EditText
                val price = priceEt.text.toString().toDoubleOrNull() ?: 0.0
                total += price
            }
        }

        val share = if (users > 0) total / users else 0.0
        Toast.makeText(
            this,
            "1인당: ${"%,.0f".format(share)}원",
            Toast.LENGTH_LONG
        ).show()
    }

}