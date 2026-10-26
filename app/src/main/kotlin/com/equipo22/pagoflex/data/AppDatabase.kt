package com.equipo22.pagoflex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CompanyEntity::class,
        UserEntity::class,
        CommitmentEntity::class,
        PaymentEntity::class,
        ReportEntity::class,
        RecommendationEntity::class,
        RecognitionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun userDao(): UserDao
    abstract fun commitmentDao(): CommitmentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun reportDao(): ReportDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun recognitionDao(): RecognitionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "pagoflex.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
