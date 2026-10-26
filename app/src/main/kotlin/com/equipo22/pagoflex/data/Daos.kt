package com.equipo22.pagoflex.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY nombreFantasia")
    fun observeAll(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies ORDER BY nombreFantasia")
    suspend fun listAll(): List<CompanyEntity>

    @Query("SELECT * FROM companies WHERE id = :id")
    fun observeById(id: Int): Flow<CompanyEntity?>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun findById(id: Int): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CompanyEntity>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY role, nombres")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE UPPER(REPLACE(REPLACE(rut, '.', ''), ' ', '')) = :rut LIMIT 1")
    suspend fun findByRut(rut: String): UserEntity?

    @Query("SELECT MAX(id) FROM users")
    suspend fun maxId(): Int?

    @Query("SELECT * FROM users WHERE role = 'EJECUTIVO' AND empresaId = :companyId LIMIT 1")
    suspend fun findExecutive(companyId: Int): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UserEntity>)

    @Insert
    suspend fun insert(item: UserEntity)

    @Query("UPDATE users SET avisosActivos = :activo, diasAnticipacion = :dias WHERE id = :id")
    suspend fun updateAlerts(id: Int, activo: Boolean, dias: Int)
}

@Dao
interface CommitmentDao {
    @Query("SELECT * FROM commitments ORDER BY fechaVencimiento")
    fun observeAll(): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE userId = :userId ORDER BY CASE estado WHEN 'Vencido' THEN 0 WHEN 'Pendiente' THEN 1 WHEN 'En revisión' THEN 2 ELSE 3 END, fechaVencimiento")
    fun observeByUser(userId: Int): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE companyId = :companyId ORDER BY fechaVencimiento DESC")
    fun observeByCompany(companyId: Int): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE id = :id")
    fun observeById(id: Long): Flow<CommitmentEntity?>

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun findById(id: Long): CommitmentEntity?

    @Query("SELECT * FROM commitments WHERE userId = :userId")
    suspend fun listByUser(userId: Int): List<CommitmentEntity>

    @Query("SELECT * FROM commitments")
    suspend fun listAll(): List<CommitmentEntity>

    @Query("SELECT * FROM commitments WHERE userId = :userId AND companyId = :companyId AND estado IN ('Pendiente', 'Vencido') ORDER BY fechaVencimiento")
    suspend fun pendingForCompany(userId: Int, companyId: Int): List<CommitmentEntity>

    @Query("SELECT * FROM commitments WHERE userId = :userId AND companyId = :companyId AND concepto = :concepto AND ((numeroCuota IS NULL AND :cuota IS NULL) OR numeroCuota = :cuota) LIMIT 1")
    suspend fun findDuplicate(userId: Int, companyId: Int, concepto: String, cuota: Int?): CommitmentEntity?

    @Query("SELECT COUNT(*) FROM commitments")
    suspend fun count(): Int

    @Insert
    suspend fun insert(item: CommitmentEntity): Long

    @Insert
    suspend fun insertAll(items: List<CommitmentEntity>)

    @Update
    suspend fun update(item: CommitmentEntity)

    @Query("UPDATE commitments SET estado = :estado, fechaCambioEstado = :fecha, recargoAplicado = :recargo WHERE id = :id")
    suspend fun updateStatus(id: Long, estado: String, fecha: String, recargo: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY id DESC")
    fun observeByUser(userId: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE companyId = :companyId ORDER BY id DESC")
    fun observeByCompany(companyId: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE commitmentId = :commitmentId LIMIT 1")
    fun observeByCommitment(commitmentId: Long): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE userId = :userId")
    suspend fun listByUser(userId: Int): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE commitmentId = :commitmentId LIMIT 1")
    suspend fun findByCommitment(commitmentId: Long): PaymentEntity?

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun count(): Int

    @Insert
    suspend fun insert(item: PaymentEntity): Long

    @Insert
    suspend fun insertAll(items: List<PaymentEntity>)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE userId = :userId ORDER BY id DESC")
    fun observeByUser(userId: Int): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE companyId = :companyId ORDER BY id DESC")
    fun observeByCompany(companyId: Int): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE commitmentId = :commitmentId ORDER BY id DESC")
    fun observeByCommitment(commitmentId: Long): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE commitmentId = :commitmentId AND estado != 'Resuelto' LIMIT 1")
    suspend fun findOpenByCommitment(commitmentId: Long): ReportEntity?

    @Query("SELECT * FROM reports WHERE id = :id")
    fun observeById(id: Long): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun findById(id: Long): ReportEntity?

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int

    @Insert
    suspend fun insert(item: ReportEntity): Long

    @Insert
    suspend fun insertAll(items: List<ReportEntity>)

    @Update
    suspend fun update(item: ReportEntity)
}

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations ORDER BY id DESC")
    fun observeAll(): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE userId = :userId ORDER BY id DESC")
    fun observeByUser(userId: Int): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE id = :id")
    suspend fun findById(id: Long): RecommendationEntity?

    @Query("SELECT * FROM recommendations WHERE userId = :userId AND lower(nombreOrganizacion) = lower(:nombre) LIMIT 1")
    suspend fun findDuplicate(userId: Int, nombre: String): RecommendationEntity?

    @Query("SELECT COUNT(*) FROM recommendations WHERE userId = :userId AND fecha LIKE :monthPattern")
    suspend fun countInMonth(userId: Int, monthPattern: String): Int

    @Query("SELECT COUNT(*) FROM recommendations")
    suspend fun count(): Int

    @Insert
    suspend fun insert(item: RecommendationEntity): Long

    @Insert
    suspend fun insertAll(items: List<RecommendationEntity>)

    @Update
    suspend fun update(item: RecommendationEntity)
}

@Dao
interface RecognitionDao {
    @Query("SELECT * FROM recognitions WHERE userId = :userId ORDER BY fechaObtencion DESC")
    fun observeByUser(userId: Int): Flow<List<RecognitionEntity>>

    @Query("SELECT COUNT(*) FROM recognitions WHERE userId = :userId AND nombre = :nombre")
    suspend fun countByName(userId: Int, nombre: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RecognitionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<RecognitionEntity>)
}

