package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // e.g. "Work", "Personal", "Health", "Shopping", "Other"
    val dueDate: String, // formatted as "YYYY-MM-DD"
    val status: String, // "Pending", "In Progress", "Completed"
    val userId: Int // ForeignKey reference to User.id
)
