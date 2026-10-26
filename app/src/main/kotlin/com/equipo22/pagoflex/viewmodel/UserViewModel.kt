package com.equipo22.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo22.pagoflex.data.CommitmentEntity
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.ComplianceInfo
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.data.PaymentEntity
import com.equipo22.pagoflex.data.RecognitionEntity
import com.equipo22.pagoflex.data.RecommendationEntity
import com.equipo22.pagoflex.data.ReportEntity
import com.equipo22.pagoflex.data.UserEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: PagoFlexRepository,
    private val userId: Int
) : ViewModel() {

    val user: StateFlow<UserEntity?> = repository.user(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val companies: StateFlow<List<CompanyEntity>> = repository.companies.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val commitments: StateFlow<List<CommitmentEntity>> = repository.commitmentsByUser(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val payments: StateFlow<List<PaymentEntity>> = repository.paymentsByUser(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val reports: StateFlow<List<ReportEntity>> = repository.reportsByUser(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val recommendations: StateFlow<List<RecommendationEntity>> = repository.recommendationsByUser(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val recognitions: StateFlow<List<RecognitionEntity>> = repository.recognitionsByUser(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val compliance: StateFlow<ComplianceInfo> = repository.compliance(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ComplianceInfo(0, 0, null, "Historial insuficiente")
    )

    fun pay(
        commitmentId: Long,
        method: String,
        photoUri: String?,
        onResult: (Result<PaymentEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.pay(commitmentId, method, photoUri))
        }
    }

    fun report(
        commitmentId: Long,
        motive: String,
        comment: String?,
        onResult: (Result<ReportEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.reportProblem(commitmentId, motive, comment))
        }
    }

    fun recommend(
        nombre: String,
        rut: String?,
        rubro: String,
        comuna: String,
        contactName: String?,
        contactEmail: String?,
        contactPhone: String?,
        authorized: Boolean,
        onResult: (Result<RecommendationEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                repository.recommend(
                    userId = userId,
                    nombre = nombre,
                    rut = rut,
                    rubro = rubro,
                    comuna = comuna,
                    contactName = contactName,
                    contactEmail = contactEmail,
                    contactPhone = contactPhone,
                    authorized = authorized
                )
            )
        }
    }

    fun updateAlerts(active: Boolean, days: Int, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(runCatching { repository.updateAlerts(userId, active, days) })
        }
    }

    suspend fun paymentBlockReason(commitment: CommitmentEntity): String? =
        repository.paymentBlockReason(commitment)

    fun paymentOf(commitmentId: Long): PaymentEntity? =
        payments.value.firstOrNull { it.commitmentId == commitmentId }

    fun companyOf(companyId: Int): CompanyEntity? =
        companies.value.firstOrNull { it.id == companyId }
}
