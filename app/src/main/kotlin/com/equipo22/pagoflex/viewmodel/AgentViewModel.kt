package com.equipo22.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo22.pagoflex.data.CommitmentEntity
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.data.PaymentEntity
import com.equipo22.pagoflex.data.RecommendationEntity
import com.equipo22.pagoflex.data.ReportEntity
import com.equipo22.pagoflex.data.UserEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(
    private val repository: PagoFlexRepository,
    private val agentId: Int
) : ViewModel() {

    val agent: StateFlow<UserEntity?> = repository.user(agentId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

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

    val commitments: StateFlow<List<CommitmentEntity>> = repository.allCommitments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val reports: StateFlow<List<ReportEntity>> = repository.allReports.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val recommendations: StateFlow<List<RecommendationEntity>> = repository.allRecommendations.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun takeReport(reportId: Long, onResult: (Result<ReportEntity>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.takeReport(reportId, agentId))
        }
    }

    fun resolveReport(
        reportId: Long,
        response: String,
        annul: Boolean,
        onResult: (Result<ReportEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.resolveReport(reportId, response, annul))
        }
    }

    fun advanceRecommendation(
        recommendationId: Long,
        status: String,
        reason: String?,
        onResult: (Result<RecommendationEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.advanceRecommendation(recommendationId, status, agentId, reason))
        }
    }
}
