package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.model.User
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (name.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("All fields are required"))
        }

        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        try {
            val existingUser = userDao.getUserByEmail(trimmedEmail)
            if (existingUser != null) {
                return Result.failure(Exception("An account with this email already exists"))
            }

            val hashedPassword = hashPassword(password)
            val newUser = User(
                name = name.trim(),
                email = trimmedEmail,
                passwordHash = hashedPassword
            )
            val id = userDao.insertUser(newUser)
            return Result.success(newUser.copy(id = id.toInt()))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password are required"))
        }

        try {
            val user = userDao.getUserByEmail(trimmedEmail) ?: return Result.failure(Exception("No account found with this email"))
            
            val hashedPassword = hashPassword(password)
            if (user.passwordHash == hashedPassword) {
                return Result.success(user)
            } else {
                return Result.failure(Exception("Incorrect password"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback in case of exception, though SHA-256 is built-into Android JUnit / JVM
        }
    }
}
