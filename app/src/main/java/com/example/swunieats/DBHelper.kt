package com.example.swunieats

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

// SQLiteOpenHelper를 상속받아 DB를 생성/업그레이드/조작할 수 있게 만든 클래스
class DBHelper(context: Context) : SQLiteOpenHelper(context, "UserDB.db", null, 1) {

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
    }

    // DB 버전이 변경되었을 때 호출됨 (앱 업데이트 등)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 기존 테이블을 삭제하고 다시 생성 (간단한 구조 변경 처리용)
        db.execSQL("DROP TABLE IF EXISTS users")
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

    //추가적으로 필요한 함수는 아래로 작성해주세요!
}