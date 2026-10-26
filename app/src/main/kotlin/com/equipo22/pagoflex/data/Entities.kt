package com.equipo22.pagoflex.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey val id: Int,
    val codigo: String,
    val razonSocial: String,
    val nombreFantasia: String,
    val rut: String,
    val rubro: String,
    val correoAtencion: String,
    val telefonoAtencion: String,
    val recargoAtraso: Long,
    val activa: Boolean = true
)

@Entity(tableName = "users", indices = [Index("role"), Index("empresaId")])
data class UserEntity(
    @PrimaryKey val id: Int,
    val code: String,
    val role: String,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val comuna: String,
    val fechaIncorporacion: String,
    val avisosActivos: Boolean = true,
    val diasAnticipacion: Int = 3,
    val area: String? = null,
    val empresaId: Int? = null,
    val cargo: String? = null,
    val activo: Boolean = true
) {
    val nombreCompleto: String get() = "$nombres $apellidos"
}

@Entity(tableName = "commitments", indices = [Index("userId"), Index("companyId"), Index("folio")])
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folio: String,
    val userId: Int,
    val companyId: Int,
    val concepto: String,
    val numeroCuota: Int? = null,
    val totalCuotas: Int? = null,
    val monto: Long,
    val fechaVencimiento: String,
    val estado: String,
    val recargoAplicado: Long = 0,
    val fechaCambioEstado: String,
    val ejecutivoRegistroId: Int? = null,
    val fechaRegistro: String,
    val anulacionMotivo: String? = null,
    val anulacionComentario: String? = null,
    val anulacionEjecutivoId: Int? = null,
    val anulacionFecha: String? = null
) {
    val totalAPagar: Long
        get() = if (estado == CommitmentStatus.VENCIDO) monto + recargoAplicado else monto
}

@Entity(tableName = "payments", indices = [Index("commitmentId"), Index("userId"), Index("companyId")])
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroComprobante: String,
    val commitmentId: Long,
    val userId: Int,
    val companyId: Int,
    val fechaHora: String,
    val montoPagado: Long,
    val canal: String,
    val medioPago: String,
    val aTiempo: Boolean,
    val fotoUri: String? = null
)

@Entity(tableName = "reports", indices = [Index("commitmentId"), Index("userId"), Index("companyId")])
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numero: String,
    val commitmentId: Long,
    val userId: Int,
    val companyId: Int,
    val motivo: String,
    val comentario: String? = null,
    val fechaCreacion: String,
    val estado: String,
    val agenteId: Int? = null,
    val fechaToma: String? = null,
    val respuesta: String? = null,
    val fechaResolucion: String? = null
)

@Entity(tableName = "recommendations", indices = [Index("userId")])
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numero: String,
    val userId: Int,
    val nombreOrganizacion: String,
    val rutOrganizacion: String? = null,
    val rubro: String,
    val comuna: String,
    val contactoNombre: String? = null,
    val contactoCorreo: String? = null,
    val contactoTelefono: String? = null,
    val autorizaContacto: Boolean = false,
    val fecha: String,
    val estado: String,
    val motivoDescarte: String? = null,
    val agenteId: Int? = null,
    val fechaCambioEstado: String? = null
)

@Entity(tableName = "recognitions", primaryKeys = ["userId", "nombre"], indices = [Index("userId")])
data class RecognitionEntity(
    val userId: Int,
    val nombre: String,
    val motivo: String,
    val fechaObtencion: String
)
