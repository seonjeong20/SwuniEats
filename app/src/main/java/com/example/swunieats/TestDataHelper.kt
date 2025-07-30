package com.example.swunieats

import android.database.sqlite.SQLiteDatabase

object TestDataHelper {

    fun insertTestData(db: SQLiteDatabase, testerId: Int) {
        if (hasTestData(db, testerId)) return

        // 1. 다른 유저도 만들어야 하므로 tester2 생성
        val tester2Id = ensureSecondTester(db)

        // 2. 채팅방 생성
        val chatroomId = insertChatroom(db, "테스트방1", "12:00", "제2과학관 1층")

        // 3. 메뉴 생성 + 주문 등록
        // tester1: 메뉴 3개
        val menu1 = insertMenu(db, chatroomId, "치킨", 18000)
        insertOrder(db, chatroomId, testerId, menu1, 1, 18000)

        val menu2 = insertMenu(db, chatroomId, "피자", 20000)
        insertOrder(db, chatroomId, testerId, menu2, 1, 20000)

        val menu3 = insertMenu(db, chatroomId, "콜라", 2000)
        insertOrder(db, chatroomId, testerId, menu3, 2, 4000)

        // tester2: 메뉴 2개
        val menu4 = insertMenu(db, chatroomId, "떡볶이", 10000)
        insertOrder(db, chatroomId, tester2Id, menu4, 1, 10000)

        val menu5 = insertMenu(db, chatroomId, "김밥", 5000)
        insertOrder(db, chatroomId, tester2Id, menu5, 2, 10000)
    }

    private fun hasTestData(db: SQLiteDatabase, userId: Int): Boolean {
        val cursor = db.rawQuery("SELECT * FROM orders WHERE userId = ?", arrayOf(userId.toString()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun insertChatroom(db: SQLiteDatabase, name: String, time: String, location: String): Int {
        db.execSQL("INSERT INTO chatrooms (name, time, location) VALUES (?, ?, ?)", arrayOf(name, time, location))
        val cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else -1
        cursor.close()
        return id
    }

    private fun insertMenu(db: SQLiteDatabase, chatroomId: Int, menuName: String, price: Int): Int {
        db.execSQL("INSERT INTO menus (chatroomId, menuName, price) VALUES (?, ?, ?)", arrayOf(chatroomId, menuName, price))
        val cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else -1
        cursor.close()
        return id
    }

    private fun insertOrder(db: SQLiteDatabase, chatroomId: Int, userId: Int, menuId: Int, quantity: Int, personalCost: Int) {
        db.execSQL(
            "INSERT INTO orders (chatroomId, userId, menuId, quantity, personalCost) VALUES (?, ?, ?, ?, ?)",
            arrayOf(chatroomId, userId, menuId, quantity, personalCost)
        )
    }

    private fun ensureSecondTester(db: SQLiteDatabase): Int {
        // tester2 ID는 9999번으로 고정 (이미 있다면 그대로 사용)
        val cursor = db.rawQuery("SELECT id FROM users WHERE id = 2", null)
        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            cursor.close()
            id
        } else {
            cursor.close()
            db.execSQL("INSERT INTO users (id, name, password) VALUES (?, ?, ?)", arrayOf(2, "tester2", "test"))
            2
        }
    }
}