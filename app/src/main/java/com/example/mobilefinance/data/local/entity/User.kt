package com.example.mobilefinance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val name: String,
    val username: String,
    val hashedPassword: String
)