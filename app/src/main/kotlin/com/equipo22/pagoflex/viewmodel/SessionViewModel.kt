package com.equipo22.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _selectedUserId = MutableStateFlow<Int?>(null)
    val selectedUserId: StateFlow<Int?> = _selectedUserId.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    val users: StateFlow<List<UserEntity>> = repository.users.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val companies: StateFlow<List<CompanyEntity>> = repository.companies.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val currentUser: StateFlow<UserEntity?> = combine(_selectedUserId, users) { id, people ->
        people.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            _ready.value = true
        }
    }

    fun select(userId: Int) {
        _selectedUserId.value = userId
    }

    fun logout() {
        _selectedUserId.value = null
    }
}
