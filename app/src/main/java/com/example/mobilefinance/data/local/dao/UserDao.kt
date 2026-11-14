package com.example.mobilefinance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.financeroom.User

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long
}
