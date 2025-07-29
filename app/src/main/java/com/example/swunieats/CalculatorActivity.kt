// CalculatorActivity.kt 파일 내용
package com.example.swunieats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.View // View import 추가
import android.widget.TextView
import android.widget.Toast
import android.content.Context

class CalculatorActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var chatroomId: Int = -1

    private lateinit var userListContainer: LinearLayout // activity_calculator.xml의 userList LinearLayout
    private lateinit var addUserButton: Button
    private lateinit var calculateButton: Button

    // 현재 로그인한 사용자의 ID (임시: 실제 앱에서는 로그인 시점에 SharedPreferences 등에서 가져와야 함)
    private var currentUserId: String = "testUser1"

    // 동적으로 추가된 user_item 뷰들을 관리할 리스트
    private val userItemViews = mutableListOf<View>() // user_item.xml의 루트는 ConstraintLayout이므로 View로 받습니다.

    override fun onCreate(savedInstanceState: Bundle?) { // Bundle?로 변경
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        dbHelper = DBHelper(this)

        // MainActivity에서 전달받은 chatroomId 가져오기
        chatroomId = intent.getIntExtra("CHATROOM_ID", -1)

        if (chatroomId == -1) {
            Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // XML 뷰 연결
        userListContainer = findViewById(R.id.userList)
        addUserButton = findViewById(R.id.addUserButton)
        calculateButton = findViewById(R.id.calculatorButton)

        // 초기 user_item 뷰를 리스트에 추가 (activity_calculator.xml에 include 된 뷰)
        // userListContainer의 첫 번째 자식이 초기 user_item 뷰입니다.
        val initialUserItemView = userListContainer.getChildAt(0)
        userItemViews.add(initialUserItemView)
        setupUserItemView(initialUserItemView, "유저1 (나)", currentUserId) // 초기 유저 설정

        // "+유저 추가" 버튼 클릭 시
        addUserButton.setOnClickListener {
            dynamicallyAddUserItemView()
        }

        // "계산하기" 버튼 클릭 시
        calculateButton.setOnClickListener {
            calculateAndDisplayCosts()
        }
    }

    // user_item 뷰를 설정하는 공통 함수
    private fun setupUserItemView(userView: View, userName: String, userId: String) {
        val tvUser = userView.findViewById<TextView>(R.id.user1) // user_item.xml 내의 user1 TextView
        tvUser.text = userName

        // 각 user_item 내의 "+메뉴 추가" 버튼 리스너 설정 (XML 양식 유지 제약으로 기능 제한)
        val addMenuButton = userView.findViewById<Button>(R.id.addMenuButton1)
        addMenuButton?.setOnClickListener {
            Toast.makeText(this, "현재는 고정된 2개의 메뉴 입력 필드만 지원합니다. 더 많은 유저를 추가해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 새로운 user_item 뷰를 동적으로 추가하는 함수
    private fun dynamicallyAddUserItemView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newUserItemView = inflater.inflate(R.layout.user_item, userListContainer, false)

        // 새로 추가된 뷰의 유저 이름 설정
        val newUserName = "유저${userItemViews.size + 1}"
        val newUserId = "user${userItemViews.size + 1}" // 임시 ID
        setupUserItemView(newUserItemView, newUserName, newUserId)

        userListContainer.addView(newUserItemView) // userList LinearLayout에 새로운 user_item 추가
        userItemViews.add(newUserItemView) // 리스트에 추가하여 관리
    }


    // 모든 메뉴와 주문 정보를 수집하고 계산하는 함수
    private fun calculateAndDisplayCosts() {
        val allOrders = mutableListOf<Map<String, Any>>() // chatroomId, userId, menuName, price, quantity

        // 각 user_item 뷰를 순회하며 메뉴 정보 수집
        userItemViews.forEachIndexed { userIndex, userView ->
            val currentUserName = (userView.findViewById<TextView>(R.id.user1))?.text.toString()

            // 실제 로그인 ID를 얻는 로직이 필요. 여기서는 임시 ID 사용.
            val currentLoginId = if (userIndex == 0) currentUserId else "user${userIndex + 1}"

            // user_item.xml에 고정된 메뉴 입력 필드 (menuName1, menuPrice1, menuName2, menuPrice2)에서 값을 가져옴
            val etMenuName1 = userView.findViewById<EditText>(R.id.menuName1)
            val etMenuPrice1 = userView.findViewById<EditText>(R.id.menuPrice1)
            val etMenuName2 = userView.findViewById<EditText>(R.id.menuName2)
            val etMenuPrice2 = userView.findViewById<EditText>(R.id.menuPrice2)

            val menuInputs = listOf(
                Pair(etMenuName1, etMenuPrice1),
                Pair(etMenuName2, etMenuPrice2)
            )

            menuInputs.forEach { (menuNameEt, menuPriceEt) ->
                val menuName = menuNameEt.text.toString().trim()
                val menuPriceStr = menuPriceEt.text.toString().trim()

                if (menuName.isNotEmpty() && menuPriceStr.isNotEmpty()) {
                    try {
                        val menuPrice = menuPriceStr.toDouble()

                        // 1. 메뉴 정보를 DB에 저장 (menus 테이블)
                        // 동일한 메뉴가 중복 저장되지 않도록 getMenuId로 확인 후 저장 가능
                        var menuId = dbHelper.getMenuId(chatroomId, menuName)
                        if (menuId == -1) { // 메뉴가 DB에 없으면 새로 삽입
                            menuId = dbHelper.insertMenu(chatroomId, menuName, menuPrice).toInt()
                        }

                        if (menuId != -1) {
                            // 2. 주문 정보를 DB에 저장 (orders 테이블)
                            val orderId = dbHelper.insertOrder(chatroomId, currentLoginId, menuId, 1) // 수량은 일단 1로 고정
                            if (orderId != -1L) {
                                allOrders.add(mapOf(
                                    "orderId" to orderId.toInt(),
                                    "userId" to currentLoginId,
                                    "userName" to currentUserName,
                                    "menuId" to menuId,
                                    "menuName" to menuName,
                                    "menuPrice" to menuPrice,
                                    "quantity" to 1
                                ))
                            } else {
                                Toast.makeText(this, "${currentUserName}의 ${menuName} 주문 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "${currentUserName}의 ${menuName} 메뉴 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "메뉴 가격은 숫자로 입력해주세요: ${menuName}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (allOrders.isEmpty()) {
            Toast.makeText(this, "입력된 메뉴가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 결제 금액 계산 및 표시 (N분의 1)
        var totalMenuCost = 0.0
        allOrders.forEach { order ->
            totalMenuCost += (order["menuPrice"] as Double) * (order["quantity"] as Int)
        }

        // 배달비 처리: XML 수정 불가 제약으로 인해 0으로 고정.
        // 실제 앱에서는 배달비를 입력받는 별도의 UI나 로직이 필요합니다.
        val deliveryFee = 0.0
        val finalTotalCost = totalMenuCost + deliveryFee

        // 사용자별 총 주문 금액 계산
        val userTotalCosts = mutableMapOf<String, Double>()
        allOrders.forEach { order ->
            val userId = order["userId"] as String
            val menuPrice = order["menuPrice"] as Double
            val quantity = order["quantity"] as Int
            userTotalCosts[userId] = (userTotalCosts[userId] ?: 0.0) + (menuPrice * quantity)
        }

        // 배달비 분배: 참여자 수로 N빵 (가장 단순한 방식)
        val distinctUsers = userTotalCosts.keys
        val numberOfUsers = distinctUsers.size
        val deliveryFeePerUser = if (numberOfUsers > 0) deliveryFee / numberOfUsers else 0.0

        val resultMessage = StringBuilder("결과:\n")
        userTotalCosts.forEach { (userId, menuCost) ->
            val finalUserCost = menuCost + deliveryFeePerUser
            resultMessage.append("${userId}: ${String.format("%,.0f원", finalUserCost)}\n")
        }

        Toast.makeText(this, resultMessage.toString(), Toast.LENGTH_LONG).show()

        // (선택 사항) 계산 후, 모든 입력 필드를 초기화하거나 화면을 종료
        // finish()
    }
}