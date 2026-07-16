package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "service_evaluations")
data class ServiceEvaluation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val delegacia: String, // Name of the police station or city
    val rating: Int,       // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ServiceEvaluationDao {
    @Query("SELECT * FROM service_evaluations ORDER BY timestamp DESC")
    fun getAllEvaluations(): Flow<List<ServiceEvaluation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: ServiceEvaluation)

    @Query("DELETE FROM service_evaluations WHERE id = :id")
    suspend fun deleteEvaluationById(id: Int)
}
