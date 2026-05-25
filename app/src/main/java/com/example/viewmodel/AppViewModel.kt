package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.model.Task
import com.example.data.model.User
import com.example.data.repository.TaskRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao)
    private val taskRepository = TaskRepository(database.taskDao)
    private val sessionManager = SessionManager(application)

    // Auth States
    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserName = MutableStateFlow(sessionManager.getUserName() ?: "")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(sessionManager.getUserEmail() ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    // Filters and Searches
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    // Active userId State driving the Task Flow
    private val _activeUserId = MutableStateFlow(sessionManager.getUserId())

    // Safe, reactive data source switching on logged-in userId state!
    @OptIn(ExperimentalCoroutinesApi::class)
    val allTasks: StateFlow<List<Task>> = _activeUserId
        .flatMapLatest { userId ->
            if (userId != -1) {
                taskRepository.getTasksForUser(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combine tasks, category, status and search query for local in-memory UI computations!
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        _searchQuery,
        _selectedCategory,
        _selectedStatus
    ) { tasks, query, category, status ->
        tasks.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || task.category.equals(category, ignoreCase = true)
            val matchesStatus = status == "All" || task.status.equals(status, ignoreCase = true)

            matchesQuery && matchesCategory && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Statistics Flow calculated reactively
    val statistics: StateFlow<TaskStats> = allTasks
        .combine(flowOf(true)) { tasks, _ ->
            val total = tasks.size
            val completed = tasks.count { it.status == "Completed" }
            val inProgress = tasks.count { it.status == "In Progress" }
            val pending = tasks.count { it.status == "Pending" }
            val rate = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
            TaskStats(total, completed, inProgress, pending, rate)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskStats(0, 0, 0, 0, 0)
        )

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            userRepository.registerUser(name, email, password)
                .onSuccess { user ->
                    sessionManager.saveSession(
                        userId = user.id,
                        name = user.name,
                        email = user.email,
                        mockToken = "session_token_jwt_${user.id}_${System.currentTimeMillis()}"
                    )
                    _activeUserId.value = user.id
                    _currentUserName.value = user.name
                    _currentUserEmail.value = user.email
                    _isLoggedIn.value = true
                    _authLoading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _authError.value = exception.message ?: "Registration failed"
                    _authLoading.value = false
                }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            userRepository.loginUser(email, password)
                .onSuccess { user ->
                    sessionManager.saveSession(
                        userId = user.id,
                        name = user.name,
                        email = user.email,
                        mockToken = "session_token_jwt_${user.id}_${System.currentTimeMillis()}"
                    )
                    _activeUserId.value = user.id
                    _currentUserName.value = user.name
                    _currentUserEmail.value = user.email
                    _isLoggedIn.value = true
                    _authLoading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _authError.value = exception.message ?: "Authentication failed"
                    _authLoading.value = false
                }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            _activeUserId.value = -1
            _currentUserName.value = ""
            _currentUserEmail.value = ""
            _isLoggedIn.value = false
            _searchQuery.value = ""
            _selectedCategory.value = "All"
            _selectedStatus.value = "All"
            onComplete()
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // Task Actions
    fun addTask(title: String, description: String, category: String, dueDate: String, status: String) {
        val userId = _activeUserId.value
        if (userId == -1 || title.isBlank()) return

        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description.trim(),
                category = category,
                dueDate = if (dueDate.isBlank()) getTodayFormatted() else dueDate,
                status = status,
                userId = userId
            )
            taskRepository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch {
            val updated = task.copy(status = newStatus)
            taskRepository.updateTask(updated)
        }
    }

    fun editTask(task: Task, title: String, description: String, category: String, dueDate: String, status: String) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title.trim(),
                description = description.trim(),
                category = category,
                dueDate = dueDate,
                status = status
            )
            taskRepository.updateTask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    // Filter updates
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    private fun getTodayFormatted(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date())
        } catch (e: Exception) {
            "2026-05-25"
        }
    }
}

// Data class for Dashboard stats
data class TaskStats(
    val total: Int,
    val completed: Int,
    val inProgress: Int,
    val pending: Int,
    val completionRate: Int
)
