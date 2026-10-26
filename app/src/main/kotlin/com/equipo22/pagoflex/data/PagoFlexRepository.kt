package com.equipo22.pagoflex.data

import com.equipo22.pagoflex.utils.addDays
import com.equipo22.pagoflex.utils.addMonths
import com.equipo22.pagoflex.utils.currentMonthKey
import com.equipo22.pagoflex.utils.formatDate
import com.equipo22.pagoflex.utils.formatDateTime
import com.equipo22.pagoflex.utils.isOnTime
import com.equipo22.pagoflex.utils.isPast
import com.equipo22.pagoflex.utils.isValidRut
import com.equipo22.pagoflex.utils.monthKeyOf
import com.equipo22.pagoflex.utils.normalizeRut
import com.equipo22.pagoflex.utils.nowDate
import com.equipo22.pagoflex.utils.nowDateTime
import com.equipo22.pagoflex.utils.parseDate
import com.equipo22.pagoflex.utils.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Date
import kotlin.math.roundToInt

data class ComplianceInfo(
    val total: Int,
    val onTime: Int,
    val percent: Double?,
    val category: String
)

class PagoFlexRepository(private val database: AppDatabase) {

    private val companyDao = database.companyDao()
    private val userDao = database.userDao()
    private val commitmentDao = database.commitmentDao()
    private val paymentDao = database.paymentDao()
    private val reportDao = database.reportDao()
    private val recommendationDao = database.recommendationDao()
    private val recognitionDao = database.recognitionDao()

    val companies: Flow<List<CompanyEntity>> = companyDao.observeAll()
    val users: Flow<List<UserEntity>> = userDao.observeAll()
    val allCommitments: Flow<List<CommitmentEntity>> = commitmentDao.observeAll()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.observeAll()
    val allReports: Flow<List<ReportEntity>> = reportDao.observeAll()
    val allRecommendations: Flow<List<RecommendationEntity>> = recommendationDao.observeAll()

    fun user(id: Int): Flow<UserEntity?> = userDao.observeById(id)
    fun company(id: Int): Flow<CompanyEntity?> = companyDao.observeById(id)
    fun commitment(id: Long): Flow<CommitmentEntity?> = commitmentDao.observeById(id)
    fun commitmentsByUser(userId: Int): Flow<List<CommitmentEntity>> = commitmentDao.observeByUser(userId)
    fun commitmentsByCompany(companyId: Int): Flow<List<CommitmentEntity>> = commitmentDao.observeByCompany(companyId)
    fun paymentsByUser(userId: Int): Flow<List<PaymentEntity>> = paymentDao.observeByUser(userId)
    fun paymentsByCompany(companyId: Int): Flow<List<PaymentEntity>> = paymentDao.observeByCompany(companyId)
    fun paymentByCommitment(commitmentId: Long): Flow<PaymentEntity?> = paymentDao.observeByCommitment(commitmentId)
    fun reportsByUser(userId: Int): Flow<List<ReportEntity>> = reportDao.observeByUser(userId)
    fun reportsByCompany(companyId: Int): Flow<List<ReportEntity>> = reportDao.observeByCompany(companyId)
    fun reportsByCommitment(commitmentId: Long): Flow<List<ReportEntity>> = reportDao.observeByCommitment(commitmentId)
    fun report(id: Long): Flow<ReportEntity?> = reportDao.observeById(id)
    fun recommendationsByUser(userId: Int): Flow<List<RecommendationEntity>> = recommendationDao.observeByUser(userId)
    fun recognitionsByUser(userId: Int): Flow<List<RecognitionEntity>> = recognitionDao.observeByUser(userId)

    fun compliance(userId: Int): Flow<ComplianceInfo> = combine(
        commitmentDao.observeByUser(userId),
        paymentDao.observeByUser(userId)
    ) { commitments, payments ->
        val reference = Date()
        val limit = addMonths(reference, -12)
        val considered = commitments.filter { commitment ->
            val due = parseDate(commitment.fechaVencimiento)
            due != null &&
                !startOfDay(due).before(startOfDay(limit)) &&
                !startOfDay(due).after(startOfDay(reference)) &&
                (commitment.estado == CommitmentStatus.PAGADO || commitment.estado == CommitmentStatus.VENCIDO)
        }
        val onTime = considered.count { commitment ->
            payments.any { it.commitmentId == commitment.id && it.aTiempo }
        }
        val percent = if (considered.size >= 3) {
            (onTime * 1000.0 / considered.size).roundToInt() / 10.0
        } else {
            null
        }
        val category = when {
            percent == null -> "Historial insuficiente"
            percent >= 95.0 -> "Ejemplar"
            percent >= 80.0 -> "Al día"
            else -> "En camino"
        }
        ComplianceInfo(considered.size, onTime, percent, category)
    }

    suspend fun findUserByRut(rut: String): UserEntity? = userDao.findByRut(normalizeRut(rut))

    suspend fun firstExecutive(companyId: Int): UserEntity? = userDao.findExecutive(companyId)

    suspend fun seedIfEmpty() {
        if (userDao.count() > 0) {
            syncOverdue()
            return
        }
        companyDao.insertAll(SeedData.companies)
        userDao.insertAll(SeedData.users)
        val inserted = mutableMapOf<String, CommitmentEntity>()
        SeedData.commitments.forEach { commitment ->
            val id = commitmentDao.insert(commitment)
            inserted[commitment.folio] = commitment.copy(id = id)
        }
        SeedData.payments.forEach { seed ->
            val commitment = inserted[seed.folio] ?: return@forEach
            paymentDao.insert(
                PaymentEntity(
                    numeroComprobante = seed.numeroComprobante,
                    commitmentId = commitment.id,
                    userId = commitment.userId,
                    companyId = commitment.companyId,
                    fechaHora = seed.fechaHora,
                    montoPagado = seed.montoPagado,
                    canal = seed.canal,
                    medioPago = seed.medioPago,
                    aTiempo = seed.aTiempo
                )
            )
        }
        SeedData.reports.forEach { seed ->
            val commitment = inserted[seed.folio] ?: return@forEach
            reportDao.insert(
                ReportEntity(
                    numero = seed.numero,
                    commitmentId = commitment.id,
                    userId = commitment.userId,
                    companyId = commitment.companyId,
                    motivo = seed.motivo,
                    comentario = seed.comentario,
                    fechaCreacion = seed.fechaCreacion,
                    estado = seed.estado,
                    agenteId = seed.agenteId,
                    fechaToma = seed.fechaToma,
                    respuesta = seed.respuesta,
                    fechaResolucion = seed.fechaResolucion
                )
            )
        }
        recommendationDao.insertAll(SeedData.recommendations)
        recognitionDao.insertAll(SeedData.recognitions)
        syncOverdue()
        generateComplianceHistory()
    }

    private suspend fun generateComplianceHistory() {
        val reference = Date()
        SeedData.complianceTargets.forEach { (userId, target) ->
            val existing = commitmentDao.listByUser(userId)
            val payments = paymentDao.listByUser(userId)
            val considered = existing.filter { commitment ->
                val due = parseDate(commitment.fechaVencimiento) ?: return@filter false
                val limit = addMonths(reference, -12)
                !startOfDay(due).before(startOfDay(limit)) &&
                    !startOfDay(due).after(startOfDay(reference)) &&
                    (commitment.estado == CommitmentStatus.PAGADO || commitment.estado == CommitmentStatus.VENCIDO)
            }
            val onTime = considered.count { commitment ->
                payments.any { it.commitmentId == commitment.id && it.aTiempo }
            }
            val missingTotal = (target.total - considered.size).coerceAtLeast(0)
            val missingOnTime = (target.onTime - onTime).coerceAtLeast(0).coerceAtMost(missingTotal)
            val companyId = existing.firstOrNull()?.companyId ?: 1
            repeat(missingTotal) { index ->
                val due = addDays(addMonths(reference, -((index % 11) + 1)), (index / 11) * 3)
                val dueText = formatDate(due)
                val amount = 12000L + (index % 6) * 4500L
                val commitment = CommitmentEntity(
                    folio = "CP-HIST-$userId-${index + 1}",
                    userId = userId,
                    companyId = companyId,
                    concepto = "Cuota histórica ${index + 1}",
                    monto = amount,
                    fechaVencimiento = dueText,
                    estado = CommitmentStatus.PAGADO,
                    recargoAplicado = 0,
                    fechaCambioEstado = dueText,
                    fechaRegistro = dueText
                )
                val commitmentId = commitmentDao.insert(commitment)
                val onTimeFlag = index < missingOnTime
                val paymentDate = if (onTimeFlag) due else addDays(due, 3)
                paymentDao.insert(
                    PaymentEntity(
                        numeroComprobante = "CPR-" + (700000 + userId * 100 + index).toString(),
                        commitmentId = commitmentId,
                        userId = userId,
                        companyId = companyId,
                        fechaHora = formatDate(paymentDate) + " 10:00",
                        montoPagado = amount,
                        canal = if (index % 2 == 0) Channels.PORTAL else Channels.AGENTE,
                        medioPago = if (index % 2 == 0) "Tarjeta de débito" else "Transferencia",
                        aTiempo = onTimeFlag
                    )
                )
            }
        }
    }

    suspend fun syncOverdue() {
        val companies = companyDao.listAll().associateBy { it.id }
        commitmentDao.listAll().forEach { commitment ->
            if (commitment.estado == CommitmentStatus.PENDIENTE) {
                val due = parseDate(commitment.fechaVencimiento) ?: return@forEach
                if (startOfDay(due).before(startOfDay(Date()))) {
                    val surcharge = companies[commitment.companyId]?.recargoAtraso ?: 0
                    commitmentDao.updateStatus(commitment.id, CommitmentStatus.VENCIDO, nowDate(), surcharge)
                }
            }
        }
    }

    suspend fun paymentBlockReason(commitment: CommitmentEntity): String? {
        when (commitment.estado) {
            CommitmentStatus.PAGADO -> return "Este compromiso ya está pagado."
            CommitmentStatus.ANULADO -> return "Este compromiso fue anulado y no se puede pagar."
            CommitmentStatus.EN_REVISION -> return "Este compromiso está en revisión. Revisa la respuesta de tu reporte."
        }
        val siblings = commitmentDao.pendingForCompany(commitment.userId, commitment.companyId)
        val older = siblings.filter { sibling ->
            sibling.id != commitment.id && dueBefore(sibling.fechaVencimiento, commitment.fechaVencimiento)
        }
        val first = older.minByOrNull { parseDate(it.fechaVencimiento)?.time ?: Long.MAX_VALUE }
        return first?.let {
            "Primero debes pagar ${it.folio} ${it.concepto}, que vence el ${it.fechaVencimiento}."
        }
    }

    private fun dueBefore(first: String, second: String): Boolean {
        val firstDate = parseDate(first) ?: return false
        val secondDate = parseDate(second) ?: return false
        return startOfDay(firstDate).before(startOfDay(secondDate))
    }

    suspend fun pay(commitmentId: Long, medioPago: String, photoUri: String?): Result<PaymentEntity> = runCatching {
        val commitment = commitmentDao.findById(commitmentId) ?: error("No encontramos el compromiso.")
        val reason = paymentBlockReason(commitment)
        if (reason != null) error(reason)
        if (medioPago.isBlank()) error("Selecciona un medio de pago.")
        val now = Date()
        val fechaHora = formatDateTime(now)
        val total = if (commitment.estado == CommitmentStatus.VENCIDO) {
            commitment.monto + commitment.recargoAplicado
        } else {
            commitment.monto
        }
        val onTime = isOnTime(fechaHora, commitment.fechaVencimiento)
        val number = "CPR-" + (paymentDao.count() + 454).toString().padStart(6, '0')
        val payment = PaymentEntity(
            numeroComprobante = number,
            commitmentId = commitment.id,
            userId = commitment.userId,
            companyId = commitment.companyId,
            fechaHora = fechaHora,
            montoPagado = total,
            canal = Channels.APP,
            medioPago = medioPago,
            aTiempo = onTime,
            fotoUri = photoUri
        )
        commitmentDao.updateStatus(commitment.id, CommitmentStatus.PAGADO, formatDate(now), commitment.recargoAplicado)
        val id = paymentDao.insert(payment)
        createPaymentRecognitions(commitment, onTime)
        payment.copy(id = id)
    }

    private suspend fun createPaymentRecognitions(commitment: CommitmentEntity, onTime: Boolean) {
        val month = monthKeyOf(commitment.fechaVencimiento)
        if (onTime) {
            recognitionDao.insert(
                RecognitionEntity(
                    userId = commitment.userId,
                    nombre = "Pago puntual $month",
                    motivo = "Pagaste a tiempo ${commitment.concepto}",
                    fechaObtencion = nowDate()
                )
            )
        }
        val monthCommitments = commitmentDao.listByUser(commitment.userId).filter {
            monthKeyOf(it.fechaVencimiento) == month &&
                it.estado != CommitmentStatus.ANULADO &&
                it.estado != CommitmentStatus.EN_REVISION
        }
        if (monthCommitments.isNotEmpty() && monthCommitments.all { it.estado == CommitmentStatus.PAGADO }) {
            val allOnTime = monthCommitments.all { item ->
                paymentDao.findByCommitment(item.id)?.aTiempo == true
            }
            if (allOnTime) {
                recognitionDao.insert(
                    RecognitionEntity(
                        userId = commitment.userId,
                        nombre = "Mes al día $month",
                        motivo = "Todos tus compromisos de $month quedaron pagados a tiempo",
                        fechaObtencion = nowDate()
                    )
                )
            }
        }
    }

    suspend fun reportProblem(commitmentId: Long, motivo: String, comentario: String?): Result<ReportEntity> = runCatching {
        val commitment = commitmentDao.findById(commitmentId) ?: error("No encontramos el compromiso.")
        if (commitment.estado != CommitmentStatus.PENDIENTE && commitment.estado != CommitmentStatus.VENCIDO) {
            error("Solo puedes reportar compromisos pendientes o vencidos.")
        }
        if (motivo.isBlank()) error("Selecciona un motivo.")
        if (motivo == "Otro" && comentario.isNullOrBlank()) error("Cuéntanos brevemente qué ocurrió.")
        val open = reportDao.findOpenByCommitment(commitmentId)
        if (open != null) error("Ya tienes un reporte abierto (${open.numero}) para este compromiso.")
        val number = "REP-" + (reportDao.count() + 13).toString().padStart(4, '0')
        val report = ReportEntity(
            numero = number,
            commitmentId = commitment.id,
            userId = commitment.userId,
            companyId = commitment.companyId,
            motivo = motivo,
            comentario = comentario?.trim()?.ifBlank { null },
            fechaCreacion = nowDateTime(),
            estado = ReportStatus.ABIERTO
        )
        val id = reportDao.insert(report)
        commitmentDao.updateStatus(commitment.id, CommitmentStatus.EN_REVISION, nowDate(), commitment.recargoAplicado)
        report.copy(id = id)
    }

    suspend fun updateAlerts(userId: Int, active: Boolean, days: Int) {
        userDao.updateAlerts(userId, active, days)
    }

    suspend fun recommend(
        userId: Int,
        nombre: String,
        rut: String?,
        rubro: String,
        comuna: String,
        contactName: String?,
        contactEmail: String?,
        contactPhone: String?,
        authorized: Boolean
    ): Result<RecommendationEntity> = runCatching {
        if (nombre.isBlank()) error("Ingresa el nombre de la organización.")
        if (rubro.isBlank()) error("Selecciona el rubro.")
        if (comuna.isBlank()) error("Selecciona la comuna.")
        if (!rut.isNullOrBlank() && !isValidRut(rut)) error("El RUT de la organización no es válido.")
        val hasContact = !contactName.isNullOrBlank() || !contactEmail.isNullOrBlank() || !contactPhone.isNullOrBlank()
        if (hasContact && !authorized) error("Debes declarar que la persona autorizó ser contactada.")
        val duplicate = recommendationDao.findDuplicate(userId, nombre.trim())
        if (duplicate != null) error("Ya recomendaste esta organización (${duplicate.numero}).")
        val month = currentMonthKey()
        val count = recommendationDao.countInMonth(userId, "%-$month")
        if (count >= 5) error("Puedes registrar hasta 5 recomendaciones por mes.")
        val number = "REC-" + (recommendationDao.count() + 7).toString().padStart(4, '0')
        val recommendation = RecommendationEntity(
            numero = number,
            userId = userId,
            nombreOrganizacion = nombre.trim(),
            rutOrganizacion = rut?.takeIf { it.isNotBlank() }?.let { normalizeRut(it) },
            rubro = rubro,
            comuna = comuna,
            contactoNombre = contactName?.trim()?.ifBlank { null },
            contactoCorreo = contactEmail?.trim()?.ifBlank { null },
            contactoTelefono = contactPhone?.trim()?.ifBlank { null },
            autorizaContacto = authorized,
            fecha = nowDate(),
            estado = RecommendationStatus.RECIBIDA
        )
        val id = recommendationDao.insert(recommendation)
        recommendation.copy(id = id)
    }

    suspend fun takeReport(reportId: Long, agentId: Int): Result<ReportEntity> = runCatching {
        val report = reportDao.findById(reportId) ?: error("No encontramos el reporte.")
        if (report.estado != ReportStatus.ABIERTO) error("Este reporte ya fue tomado.")
        val updated = report.copy(estado = ReportStatus.EN_GESTION, agenteId = agentId, fechaToma = nowDateTime())
        reportDao.update(updated)
        updated
    }

    suspend fun resolveReport(reportId: Long, response: String, annul: Boolean): Result<ReportEntity> = runCatching {
        val report = reportDao.findById(reportId) ?: error("No encontramos el reporte.")
        if (report.estado != ReportStatus.EN_GESTION) error("Primero debes tomar el reporte.")
        if (response.isBlank()) error("Escribe una respuesta para la persona.")
        val commitment = commitmentDao.findById(report.commitmentId) ?: error("No encontramos el compromiso.")
        if (annul) {
            val executive = userDao.findExecutive(commitment.companyId)
            commitmentDao.update(
                commitment.copy(
                    estado = CommitmentStatus.ANULADO,
                    fechaCambioEstado = nowDate(),
                    anulacionMotivo = "Pagado directamente a la empresa",
                    anulacionComentario = "Anulación coordinada con la empresa cliente",
                    anulacionEjecutivoId = executive?.id,
                    anulacionFecha = nowDate()
                )
            )
        } else {
            val newState = if (isPast(commitment.fechaVencimiento)) CommitmentStatus.VENCIDO else CommitmentStatus.PENDIENTE
            val company = companyDao.findById(commitment.companyId)
            val surcharge = if (newState == CommitmentStatus.VENCIDO) company?.recargoAtraso ?: commitment.recargoAplicado else 0
            commitmentDao.update(
                commitment.copy(
                    estado = newState,
                    fechaCambioEstado = nowDate(),
                    recargoAplicado = surcharge
                )
            )
        }
        val updated = report.copy(
            estado = ReportStatus.RESUELTO,
            respuesta = response.trim(),
            fechaResolucion = nowDateTime(),
            agenteId = report.agenteId ?: 101
        )
        reportDao.update(updated)
        updated
    }

    suspend fun advanceRecommendation(
        recommendationId: Long,
        newStatus: String,
        agentId: Int,
        reason: String?
    ): Result<RecommendationEntity> = runCatching {
        val recommendation = recommendationDao.findById(recommendationId)
            ?: error("No encontramos la recomendación.")
        if (recommendation.estado == RecommendationStatus.SE_UNIO || recommendation.estado == RecommendationStatus.DESCARTADA) {
            error("Esta recomendación ya está cerrada.")
        }
        val allowed = when (newStatus) {
            RecommendationStatus.CONTACTADA -> recommendation.estado == RecommendationStatus.RECIBIDA
            RecommendationStatus.EN_EVALUACION -> recommendation.estado == RecommendationStatus.CONTACTADA
            RecommendationStatus.SE_UNIO -> recommendation.estado == RecommendationStatus.EN_EVALUACION
            RecommendationStatus.DESCARTADA -> recommendation.estado != RecommendationStatus.SE_UNIO
            else -> false
        }
        if (!allowed) error("Debes avanzar por las etapas sin saltarte ninguna.")
        if (newStatus == RecommendationStatus.DESCARTADA && reason.isNullOrBlank()) {
            error("Indica el motivo del descarte.")
        }
        val updated = recommendation.copy(
            estado = newStatus,
            motivoDescarte = if (newStatus == RecommendationStatus.DESCARTADA) reason?.trim() else null,
            agenteId = agentId,
            fechaCambioEstado = nowDateTime()
        )
        recommendationDao.update(updated)
        if (newStatus == RecommendationStatus.SE_UNIO) {
            recognitionDao.insert(
                RecognitionEntity(
                    userId = recommendation.userId,
                    nombre = "Embajador PagoFlex",
                    motivo = "La organización ${recommendation.nombreOrganizacion} se unió a PagoFlex",
                    fechaObtencion = nowDate()
                )
            )
        }
        updated
    }

    suspend fun registerCommitment(
        executiveId: Int,
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
        totalInstallments: Int?
    ): Result<CommitmentEntity> = runCatching {
        val executive = userDao.findById(executiveId) ?: error("No encontramos al ejecutivo.")
        if (executive.empresaId != companyId) error("No puedes registrar compromisos de otra empresa.")
        if (!isValidRut(rut)) error("El RUT de la persona no es válido.")
        if (concept.isBlank()) error("Ingresa el concepto del compromiso.")
        if (amount <= 0) error("El monto debe ser mayor que cero.")
        val due = parseDate(dueDate) ?: error("Usa una fecha válida con formato DD-MM-AAAA.")
        if (startOfDay(due).before(startOfDay(Date()))) error("La fecha de vencimiento no puede ser anterior a hoy.")
        if ((installment == null) != (totalInstallments == null)) {
            error("Completa el número y el total de cuotas, o deja ambos vacíos.")
        }
        if (installment != null && totalInstallments != null && installment > totalInstallments) {
            error("La cuota no puede ser mayor que el total de cuotas.")
        }
        var person = userDao.findByRut(normalizeRut(rut))
        if (person == null) {
            if (names.isBlank() || surnames.isBlank()) error("Ingresa nombres y apellidos de la persona.")
            if (email.isBlank() || !email.contains("@")) error("Ingresa un correo válido.")
            if (commune.isBlank()) error("Selecciona la comuna de la persona.")
            val newId = (userDao.maxId() ?: 0) + 1
            person = UserEntity(
                id = newId,
                code = "UF-$newId",
                role = Roles.USUARIO_FINAL,
                rut = normalizeRut(rut),
                nombres = names.trim(),
                apellidos = surnames.trim(),
                correo = email.trim(),
                telefono = phone.trim(),
                comuna = commune,
                fechaIncorporacion = nowDate(),
                avisosActivos = true,
                diasAnticipacion = 3
            )
            userDao.insert(person)
        }
        val duplicate = commitmentDao.findDuplicate(person.id, companyId, concept.trim(), installment)
        if (duplicate != null) error("Ya existe este compromiso (${duplicate.folio}).")
        val folio = "CP-2026-" + (commitmentDao.count() + 113).toString().padStart(4, '0')
        val commitment = CommitmentEntity(
            folio = folio,
            userId = person.id,
            companyId = companyId,
            concepto = concept.trim(),
            numeroCuota = installment,
            totalCuotas = totalInstallments,
            monto = amount,
            fechaVencimiento = dueDate,
            estado = CommitmentStatus.PENDIENTE,
            recargoAplicado = 0,
            fechaCambioEstado = nowDate(),
            ejecutivoRegistroId = executiveId,
            fechaRegistro = nowDate()
        )
        val id = commitmentDao.insert(commitment)
        commitment.copy(id = id)
    }

    suspend fun annulCommitment(
        commitmentId: Long,
        executiveId: Int,
        reason: String,
        comment: String?
    ): Result<CommitmentEntity> = runCatching {
        val executive = userDao.findById(executiveId) ?: error("No encontramos al ejecutivo.")
        val commitment = commitmentDao.findById(commitmentId) ?: error("No encontramos el compromiso.")
        if (commitment.companyId != executive.empresaId) error("Solo puedes anular compromisos de tu empresa.")
        if (commitment.estado == CommitmentStatus.PAGADO) error("Un compromiso pagado no se puede anular.")
        if (commitment.estado == CommitmentStatus.ANULADO) error("Este compromiso ya está anulado.")
        if (reason.isBlank()) error("Selecciona un motivo de anulación.")
        if (reason == "Otro" && comment.isNullOrBlank()) error("Cuéntanos el motivo de la anulación.")
        val updated = commitment.copy(
            estado = CommitmentStatus.ANULADO,
            fechaCambioEstado = nowDate(),
            anulacionMotivo = reason,
            anulacionComentario = comment?.trim()?.ifBlank { null },
            anulacionEjecutivoId = executiveId,
            anulacionFecha = nowDate()
        )
        commitmentDao.update(updated)
        updated
    }
}
