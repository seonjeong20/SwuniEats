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

    private lateinit var db: DBHelper
    private var chatroomId: Int = -1
    private var userCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        // --- view binding ---
        scrollView    = findViewById(R.id.scrollView)
        container     = findViewById(R.id.userBlocksContainer)
        etDeliveryFee = findViewById(R.id.etDeliveryFee)
        btnAddUser    = findViewById(R.id.btnAddUser)
        btnCalculate  = findViewById(R.id.btnCalculate)
        tvResult      = findViewById(R.id.tvResult)

        db = DBHelper(this)
        chatroomId = intent.getIntExtra("CHATROOM_ID", -1)

        // 1) 저장된 데이터가 있으면 로드
        if (chatroomId != -1) {
            loadSavedData()
        }

        // 2) 버튼 리스너
        btnAddUser.setOnClickListener {
            addUserBlock()
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }
        btnCalculate.setOnClickListener {
            calculateAndSave()
        }

        // 3) 최소 한 명의 사용자 블록은 표시
        if (container.childCount == 0) {
            addUserBlock()
        }
    }

    /**
     * DB에 저장된 배달비, 메뉴·주문 정보를 불러와서 UI에 재구성
     * 소수점 없이 Int 형태로 불러옵니다.
     */
    private fun loadSavedData() {
        // 저장된 배달비를 불러와 Int로 반올림
        val savedFee = db.getDeliveryFee(chatroomId).roundToInt()
        etDeliveryFee.setText(savedFee.toString())

        val menus  = db.getMenusForChatroom(chatroomId)
        val orders = db.getOrdersForChatroom(chatroomId)
        if (orders.isEmpty()) return

        container.removeAllViews()
        userCount = 0

        // userId별로 묶어서 UI 재구성
        orders.groupBy { it.userId }.forEach { (userId, userOrders) ->
            val block = addUserBlock(userId)
            val menuContainer = block.getChildAt(1) as LinearLayout
            userOrders.forEach { order ->
                val menu = menus.firstOrNull { it.id == order.menuId }
                addMenuField(
                    menuContainer,
                    menu?.menuName ?: "",
                    menu?.price?.roundToInt()?.toString() ?: ""
                )
            }
        }
    }

    /**
     * UI의 입력값을 DB에 저장하고, 결과를 계산하여 화면에 표시
     * etDeliveryFee는 Int로 파싱하여 사용합니다.
     */
    private fun calculateAndSave() {
        // 배달비를 Int로 파싱
        val feeInt = etDeliveryFee.text.toString().toIntOrNull() ?: 0

        // 기존 데이터 제거
        db.deleteOrdersForChatroom(chatroomId)
        db.deleteMenusForChatroom(chatroomId)

        // 새 배달비 저장 (Double로 변환)
        db.setDeliveryFee(chatroomId, feeInt.toDouble())

        val count = container.childCount
        for (i in 0 until count) {
            val block = container.getChildAt(i) as ViewGroup
            val tvUser = block.getChildAt(0) as TextView
            val menuContainer = block.getChildAt(1) as LinearLayout

            // ① 각 메뉴 가격 합산 (Double 합 → Int 반올림)
            var sumPrice = 0.0
            for (j in 0 until menuContainer.childCount step 2) {
                val priceEt = menuContainer.getChildAt(j + 1) as EditText
                sumPrice += priceEt.text.toString().toDoubleOrNull() ?: 0.0
            }
            val sumInt = sumPrice.roundToInt()

            // ② 1/n 분담비 (Int 나눗셈)
            val shareFee = if (count > 0) feeInt / count else 0

            // ③ DB 저장
            for (j in 0 until menuContainer.childCount step 2) {
                val nameEt  = menuContainer.getChildAt(j)     as EditText
                val priceEt = menuContainer.getChildAt(j + 1) as EditText
                val price   = priceEt.text.toString().toDoubleOrNull() ?: 0.0

                val menuId  = db.insertMenu(chatroomId, nameEt.text.toString(), price)
                val orderId = db.insertOrder(chatroomId, tvUser.text.toString(), menuId)

                // 개인부담금: sumInt + shareFee
                db.updatePersonalCost(orderId, (sumInt + shareFee).toDouble())
            }
        }

        // ④ 계산 결과 표시
        calculate()
    }

    /**
     * UI에 입력된 값을 기반으로 1/n 계산 후 결과만 보여주기
     * 모든 금액을 Int 형식(원 단위)으로 표시합니다.
     */
    private fun calculate() {
        val feeInt = etDeliveryFee.text.toString().toIntOrNull() ?: 0
        val count = container.childCount
        val shareFee = if (count > 0) feeInt / count else 0
        val sb = StringBuilder()

        for (i in 0 until count) {
            val block = container.getChildAt(i) as ViewGroup
            val tvUser = block.getChildAt(0) as TextView
            val menuContainer = block.getChildAt(1) as LinearLayout

            // 메뉴 가격 합산
            var sumPrice = 0.0
            for (j in 0 until menuContainer.childCount step 2) {
                val priceEt = menuContainer.getChildAt(j + 1) as EditText
                sumPrice += priceEt.text.toString().toDoubleOrNull() ?: 0.0
            }
            val total = sumPrice.roundToInt() + shareFee
            sb.append("${tvUser.text}: %,d원\n".format(total))
        }

        tvResult.text = sb.toString().trimEnd()
    }

    /**
     * 사용자 블록 생성
     * @param userName 지정된 이름(또는 null인 경우 자동 부여)
     */
    private fun addUserBlock(userName: String? = null): LinearLayout {
        userCount++
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 사용자 이름
        val tvUser = TextView(this).apply {
            text = userName ?: "User $userCount"
            textSize = 16f
        }
        block.addView(tvUser)

        // 메뉴 입력 컨테이너
        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        block.addView(menuContainer)

        // “메뉴 추가” 버튼
        val btnAddMenu = Button(this).apply {
            text = "메뉴 추가"
            setOnClickListener { addMenuField(menuContainer) }
        }
        block.addView(btnAddMenu)

        container.addView(block)
        return block
    }

    /**
     * 메뉴 이름/가격 입력 필드 추가
     */
    private fun addMenuField(
        menuContainer: LinearLayout,
        name: String = "",
        price: String = ""
    ) {
        // 메뉴명
        val etName = EditText(this).apply {
            hint = "메뉴 이름"
            setText(name)
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        menuContainer.addView(etName)

        // 가격
        val etPrice = EditText(this).apply {
            hint = "가격"
            setText(price)
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        menuContainer.addView(etPrice)
    }
}
