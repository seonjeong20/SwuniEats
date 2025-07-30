package com.example.swunieats

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "UserDB.db"
        const val DB_VERSION = 2

        // --- users table ---
        const val TABLE_USERS    = "users"
        const val COL_USER_ID    = "id"
        const val COL_USER_NAME  = "name"
        const val COL_USER_PW    = "password"

        // --- chatrooms table ---
        const val TABLE_CHATROOMS   = "chatrooms"
        const val COL_CHAT_ID       = "id"
        const val COL_CHAT_NAME     = "name"
        const val COL_CHAT_TIME     = "time"
        const val COL_CHAT_LOCATION = "location"

        // --- menus table ---
        const val TABLE_MENUS       = "menus"
        const val COL_MENU_ID       = "id"
        const val COL_MENU_CHAT_ID  = "chatroomId"
        const val COL_MENU_NAME     = "menuName"
        const val COL_MENU_PRICE    = "price"

        // --- orders table ---
        const val TABLE_ORDERS      = "orders"
        const val COL_ORDER_ID      = "id"
        const val COL_ORDER_CHAT_ID = "chatroomId"
        const val COL_ORDER_USER_ID = "userId"
        const val COL_ORDER_MENU_ID = "menuId"
        const val COL_ORDER_QTY     = "quantity"
        const val COL_ORDER_COST    = "personalCost"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // users
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID   TEXT PRIMARY KEY,
                $COL_USER_NAME TEXT,
                $COL_USER_PW   TEXT
            )
        """.trimIndent())

        // chatrooms
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CHATROOMS (
                $COL_CHAT_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHAT_NAME     TEXT,
                $COL_CHAT_TIME     TEXT,
                $COL_CHAT_LOCATION TEXT
            )
        """.trimIndent())

        // menus
        db.execSQL("""
            CREATE TABLE $TABLE_MENUS (
                $COL_MENU_ID      INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MENU_CHAT_ID INTEGER NOT NULL,
                $COL_MENU_NAME    TEXT NOT NULL,
                $COL_MENU_PRICE   REAL NOT NULL
            )
        """.trimIndent())

        // orders
        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                $COL_ORDER_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ORDER_CHAT_ID  INTEGER NOT NULL,
                $COL_ORDER_USER_ID  TEXT NOT NULL,
                $COL_ORDER_MENU_ID  INTEGER NOT NULL,
                $COL_ORDER_QTY      INTEGER DEFAULT 1,
                $COL_ORDER_COST     REAL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHATROOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MENUS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    // --- Users ---
    fun insertUser(id: String, name: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, id)
            put(COL_USER_NAME, name)
            put(COL_USER_PW, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(id: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USER_ID = ? AND $COL_USER_PW = ?",
            arrayOf(id, password),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun isIdDuplicate(id: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USER_ID = ?",
            arrayOf(id),
            null, null, null
        )
        val duplicate = cursor.count > 0
        cursor.close()
        db.close()
        return duplicate
    }

    // --- Chatrooms ---
    fun insertChatroom(name: String, time: String, location: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CHAT_NAME, name)
            put(COL_CHAT_TIME, time)
            put(COL_CHAT_LOCATION, location)
        }
        val id = db.insert(TABLE_CHATROOMS, null, values)
        db.close()
        return id
    }

    fun getAllChatrooms(): List<Chatroom> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CHATROOMS,
            arrayOf(COL_CHAT_ID, COL_CHAT_NAME, COL_CHAT_TIME, COL_CHAT_LOCATION),
            null, null, null, null, "$COL_CHAT_ID ASC"
        )
        val list = mutableListOf<Chatroom>()
        while (cursor.moveToNext()) {
            list += Chatroom(
                id       = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAT_ID)),
                name     = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_NAME)),
                time     = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TIME)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_LOCATION))
            )
        }
        cursor.close()
        db.close()
        return list
    }

    // --- Menus ---
    fun insertMenu(chatroomId: Int, menuName: String, price: Double): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_MENU_CHAT_ID, chatroomId)
            put(COL_MENU_NAME, menuName)
            put(COL_MENU_PRICE, price)
        }
        val id = db.insert(TABLE_MENUS, null, values)
        db.close()
        return id
    }

    fun getMenusForChatroom(chatroomId: Int): List<Menu> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MENUS,
            arrayOf(COL_MENU_ID, COL_MENU_NAME, COL_MENU_PRICE),
            "$COL_MENU_CHAT_ID = ?",
            arrayOf(chatroomId.toString()),
            null, null, "$COL_MENU_ID ASC"
        )
        val list = mutableListOf<Menu>()
        while (cursor.moveToNext()) {
            list += Menu(
                id         = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MENU_ID)),
                chatroomId = chatroomId,
                menuName   = cursor.getString(cursor.getColumnIndexOrThrow(COL_MENU_NAME)),
                price      = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_MENU_PRICE))
            )
        }
        cursor.close()
        db.close()
        return list
    }

    // --- Orders ---
    fun insertOrder(
        chatroomId: Int,
        userId: String,
        menuId: Int,
        quantity: Int = 1
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ORDER_CHAT_ID, chatroomId)
            put(COL_ORDER_USER_ID, userId)
            put(COL_ORDER_MENU_ID, menuId)
            put(COL_ORDER_QTY, quantity)
        }
        val id = db.insert(TABLE_ORDERS, null, values)
        db.close()
        return id
    }

    fun updatePersonalCost(orderId: Int, cost: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ORDER_COST, cost)
        }
        val count = db.update(
            TABLE_ORDERS,
            values,
            "$COL_ORDER_ID = ?",
            arrayOf(orderId.toString())
        )
        db.close()
        return count
    }
}
