package de.paul.asjabin.terminverwaltung

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun observeAppointments(): Flow<List<Appointment>>

    @Insert
    suspend fun insert(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appointments: List<Appointment>)

    @Update
    suspend fun update(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun deleteById(appointmentId: Int)
}
