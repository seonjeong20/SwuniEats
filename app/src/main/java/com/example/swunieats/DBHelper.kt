package com.example.swunieats

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

// SQLiteOpenHelper를 상속받아 DB를 생성/업그레이드/조작할 수 있게 만든 클래스
class DBHelper(context: Context) : SQLiteOpenHelper(context, "UserDB.db", null, 2) {

    // 앱이 처음 설치될 때 실행됨 → 사용자 테이블 생성
    override fun onCreate(db: SQLiteDatabase) {
        // users 테이블 생성: id(아이디), name(이름), password(비밀번호)
        // id는 PRIMARY KEY로 설정하여 중복 불가
        db.execSQL(
            "CREATE TABLE users (" +
                    "id TEXT PRIMARY KEY, " +    // 고유 아이디 (중복 불가)
                    "name TEXT, " +              // 사용자 이름
                    "password TEXT)"             // 비밀번호
        )

        // chatrooms 테이블 생성
        db.execSQL(
            """
        CREATE TABLE IF NOT EXISTS chatrooms (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            time TEXT,
            location TEXT
        )
        """
        )
        db.execSQL(
            """
            CREATE TABLE menus (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chatroomId INTEGER NOT NULL,
                menuName TEXT NOT NULL,
                price REAL NOT NULL
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                chatroomId INTEGER NOT NULL,
                userId TEXT NOT NULL,
                menuId INTEGER NOT NULL,
                quantity INTEGER DEFAULT 1,
                personalCost REAL
            )
            """
        )
    }

    // DB 버전이 변경되었을 때 호출됨 (앱 업데이트 등)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 기존 테이블을 삭제하고 다시 생성 (간단한 구조 변경 처리용)
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS chatrooms")
        db.execSQL("DROP TABLE IF EXISTS menus")
        db.execSQL("DROP TABLE IF EXISTS orders")
        onCreate(db)
    }

    // 회원가입 시 사용자 정보를 테이블에 저장하는 함수
    fun insertUser(id: String, name: String, password: String): Boolean {
        val db = this.writableDatabase // 쓰기 가능한 DB 가져오기

        // ContentValues를 이용해 key-value 형태로 데이터 구성
        val values = ContentValues()
        values.put("id", id)
        values.put("name", name)
        values.put("password", password)

        // insert()는 성공하면 삽입된 행의 row ID를, 실패하면 -1을 반환함
        val result = db.insert("users", null, values)
        return result != -1L  // 삽입 성공 여부를 true/false로 반환
    }

    // 로그인 시 사용자의 아이디와 비밀번호가 일치하는지 확인
    fun checkUser(id: String, password: String): Boolean {
        val db = this.readableDatabase // 읽기 전용 DB 가져오기

        // 해당 아이디와 비밀번호가 모두 일치하는 행을 조회
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM users WHERE id = ? AND password = ?",
            arrayOf(id, password)
        )

        val exists = cursor.count > 0 // 일치하는 행이 하나라도 있다면 true
        cursor.close()
        return exists
    }

    // 아이디 중복 확인 함수
    fun isIdDuplicate(id: String): Boolean {
        val db = this.readableDatabase // 읽기 전용 DB 가져오기

        // 같은 아이디가 이미 DB에 존재하는지 확인
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM users WHERE id = ?",
            arrayOf(id)
        )

        val duplicate = cursor.count > 0 // 존재하면 true, 아니면 false
        cursor.close()
        return duplicate
    }

    // 채팅방 목록 가져오기
    fun getAllChatrooms(): List<Chatroom> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM chatrooms", null)
        val result = mutableListOf<Chatroom>()

        val idColumnIndex = cursor.getColumnIndexOrThrow("id")
        val nameColumnIndex = cursor.getColumnIndexOrThrow("name")
        val timeColumnIndex = cursor.getColumnIndexOrThrow("time")
        val locationColumnIndex = cursor.getColumnIndexOrThrow("location")

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val time = cursor.getString(2)
            val location = cursor.getString(3)
            result.add(Chatroom(id, name, time, location))
        }
        cursor.close()
        return result
    }

    //추가적으로 필요한 함수는 아래로 작성해주세요!

    fun insertChatroom(name: String, time: String, location: String): Long { // <<== 이 부분이 가장 중요합니다. deliveryFee: Double 이 없어야 합니다.
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("time", time)
            put("location", location)

        }
        return db.insert("chatrooms", null, values)
    }

    // 메뉴 추가 함수
    fun insertMenu(chatroomId: Int, menuName: String, price: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("chatroomId", chatroomId)
            put("menuName", menuName)
            put("price", price)
        }
        return db.insert("menus", null, values)
    }

    // 특정 채팅방의 모든 메뉴 가져오기 (Map 형태로 반환)
    fun getMenusForChatroom(chatroomId: Int): List<Map<String, Any>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, chatroomId, menuName, price FROM menus WHERE chatroomId = ?", arrayOf(chatroomId.toString()))
        val menuList = mutableListOf<Map<String, Any>>()
        val idColumnIndex = cursor.getColumnIndexOrThrow("id")
        val chatroomIdColumnIndex = cursor.getColumnIndexOrThrow("chatroomId")
        val menuNameColumnIndex = cursor.getColumnIndexOrThrow("menuName")
        val priceColumnIndex = cursor.getColumnIndexOrThrow("price")

        while (cursor.moveToNext()) {
            val menuMap = mutableMapOf<String, Any>()
            menuMap["id"] = cursor.getInt(idColumnIndex)
            menuMap["chatroomId"] = cursor.getInt(chatroomIdColumnIndex)
            menuMap["menuName"] = cursor.getString(menuNameColumnIndex)
            menuMap["price"] = cursor.getDouble(priceColumnIndex)
            menuList.add(menuMap)
        }
        cursor.close()
        return menuList
    }

    // 주문 추가 함수
    fun insertOrder(chatroomId: Int, userId: String, menuId: Int, quantity: Int = 1): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("chatroomId", chatroomId)
            put("userId", userId)
            put("menuId", menuId)
            put("quantity", quantity)
            // personalCost는 나중에 계산 후 업데이트
        }
        return db.insert("orders", null, values)
    }

    // 특정 채팅방의 모든 주문 정보 가져오기 (사용자, 메뉴 이름 등과 조인하여 Map 형태로 반환)
    fun getOrdersDetailsForChatroom(chatroomId: Int): List<Map<String, Any>> {
        val db = readableDatabase
        val query = """
        SELECT
            O.id AS orderId, O.userId, O.quantity, O.personalCost,
            M.menuName, M.price,
            U.name AS userName, U.id AS userLoginId
        FROM orders O
        INNER JOIN menus M ON O.menuId = M.id
        INNER JOIN users U ON O.userId = U.id
        WHERE O.chatroomId = ?
    """
        val cursor = db.rawQuery(query, arrayOf(chatroomId.toString()))
        val orderDetailsList = mutableListOf<Map<String, Any>>()

        while (cursor.moveToNext()) {
            val orderMap = mutableMapOf<String, Any>()
            orderMap["orderId"] = cursor.getInt(cursor.getColumnIndexOrThrow("orderId"))
            orderMap["userId"] = cursor.getString(cursor.getColumnIndexOrThrow("userId"))
            orderMap["quantity"] = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
            val personalCostIndex = cursor.getColumnIndex("personalCost")
            if (personalCostIndex != -1 && !cursor.isNull(personalCostIndex)) {
                orderMap["personalCost"] = cursor.getDouble(personalCostIndex)
            } else {
                orderMap["personalCost"] = 0.0 // 또는 null
            }
            orderMap["menuName"] = cursor.getString(cursor.getColumnIndexOrThrow("menuName"))
            orderMap["menuPrice"] = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
            orderMap["userName"] = cursor.getString(cursor.getColumnIndexOrThrow("userName"))
            orderMap["userLoginId"] = cursor.getString(cursor.getColumnIndexOrThrow("userLoginId"))
            orderDetailsList.add(orderMap)
        }
        cursor.close()
        return orderDetailsList
    }

    // 개인 부담 금액 업데이트
    fun updatePersonalCost(orderId: Int, cost: Double): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("personalCost", cost)
        }
        return db.update("orders", values, "id = ?", arrayOf(orderId.toString()))
    }

    // 특정 메뉴의 ID 가져오기 (메뉴 이름과 채팅방 ID로)
    fun getMenuId(chatroomId: Int, menuName: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM menus WHERE chatroomId = ? AND menuName = ?", arrayOf(chatroomId.toString(), menuName))
        var menuId = -1
        if (cursor.moveToFirst()) {
            menuId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        return menuId
    }
}