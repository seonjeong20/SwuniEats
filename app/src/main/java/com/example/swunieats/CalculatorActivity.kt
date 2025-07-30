package com.example.swunieats

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class CalculatorActivity : AppCompatActivity() {
    private lateinit var scrollView: ScrollView
    private lateinit var container: LinearLayout
    private lateinit var etDeliveryFee: EditText
    private lateinit var btnAddUser: Button
    private lateinit var btnCalculate: Button
    private lateinit var tvResult: TextView
    private var userCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        scrollView = findViewById(R.id.scrollView)
        container = findViewById(R.id.userBlocksContainer)
        etDeliveryFee = findViewById(R.id.etDeliveryFee)
        btnAddUser = findViewById(R.id.btnAddUser)
        btnCalculate = findViewById(R.id.btnCalculate)
        tvResult = findViewById(R.id.tvResult)

        btnAddUser.setOnClickListener {
            addUserBlock()
            // 새 블록 추가 후 스크롤 다운
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }
        btnCalculate.setOnClickListener { calculate() }

        // 초기 사용자 블록 하나 추가
        addUserBlock()
    }

    private fun addUserBlock() {
        userCount++
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val tvUser = TextView(this).apply {
            text = "User $userCount"
            textSize = 16f
        }
        block.addView(tvUser)

        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        block.addView(menuContainer)

        val btnMenu = Button(this).apply {
            text = "메뉴 추가"
            setOnClickListener {
                addMenuField(menuContainer)
                // 새 메뉴 필드 추가 후 스크롤 다운
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        }
        block.addView(btnMenu)

        container.addView(block)
    }

    private fun addMenuField(container: LinearLayout) {
        val etName = EditText(this).apply {
            hint = "메뉴 이름"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val etPrice = EditText(this).apply {
            hint = "가격"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(etName)
        container.addView(etPrice)
    }

    private fun calculate() {
        val fee = etDeliveryFee.text.toString().toDoubleOrNull() ?: 0.0
        val users = container.childCount
        val shareFee = if (users > 0) (fee / users).roundToInt() else 0

        val sb = StringBuilder()
        for (i in 0 until users) {
            val block = container.getChildAt(i) as ViewGroup
            val menuCont = block.getChildAt(1) as LinearLayout
            var sumPrice = 0.0
            val fields = menuCont.childCount
            for (j in 0 until fields step 2) {
                val priceEt = menuCont.getChildAt(j + 1) as EditText
                val price = priceEt.text.toString().toDoubleOrNull() ?: 0.0
                sumPrice += price
            }
            val total = sumPrice.roundToInt() + shareFee
            sb.append("${i+1}번 사용자: %,d원\n".format(total))
        }
        tvResult.text = sb.toString().trimEnd()
    }
}