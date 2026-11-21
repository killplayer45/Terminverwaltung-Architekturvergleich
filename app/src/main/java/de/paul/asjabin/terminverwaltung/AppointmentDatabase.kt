package de.paul.asjabin.terminverwaltung

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [Appointment::class], version = 1, exportSchema = false)
abstract class AppointmentDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        private const val DATABASE_NAME = "appointments.db"

        @Volatile
        private var instance: AppointmentDatabase? = null

        fun getDatabase(context: Context): AppointmentDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        private fun buildDatabase(context: Context): AppointmentDatabase =
            Room.databaseBuilder(
                context,
                AppointmentDatabase::class.java,
                DATABASE_NAME
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        getDatabase(context).appointmentDao().insertAll(sampleAppointments)
                    }
                }
            }).build()
    }
}