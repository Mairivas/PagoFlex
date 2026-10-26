package com.equipo22.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo22.pagoflex.data.CommitmentEntity
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.data.PaymentEntity
import com.equipo22.pagoflex.data.ReportEntity
import com.equipo22.pagoflex.data.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExecutiveViewModel(
    private val repository: PagoFlexRepository,
    private val executiveId: Int
) : ViewModel() {

    val executive: StateFlow<UserEntity?> = repository.user(executiveId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    private val companyId = executive.map { it?.empresaId }

    val company: StateFlow<CompanyEntity?> = companyId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.company(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val commitments: StateFlow<List<CommitmentEntity>> = companyId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.commitmentsByCompany(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = companyId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.paymentsByCompany(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reports: StateFlow<List<ReportEntity>> = companyId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.reportsByCompany(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.users.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun findPerson(rut: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.findUserByRut(rut))
        }
    }

    fun registerCommitment(
        companyId: Int,
        rut: String,
        names: String,
        surnames: String,
        email: String,
        phone: String,
        commune: String,
        concept: String,
        amount: Long,
        dueDate: String,
        installment: Int?,
        totalInstallments: Int?,
        onResult: (Result<CommitmentEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                repository.registerCommitment(
                    executiveId = executiveId,
                    companyId = companyId,
                    rut = rut,
                    names = names,
                    surnames = surnames,
                    email = email,
                    phone = phone,
                    commune = commune,
                    concept = concept,
                    amount = amount,
                    dueDate = dueDate,
                    installment = installment,
                    totalInstallments = totalInstallments
                )
            )
        }
    }

    fun annulCommitment(
        commitmentId: Long,
        reason: String,
        comment: String?,
        onResult: (Result<CommitmentEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.annulCommitment(commitmentId, executiveId, reason, comment))
        }
    }
}
