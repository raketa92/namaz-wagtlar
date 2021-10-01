package com.github.raketa92.namazwagtlar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.raketa92.namazwagtlar.dao.NamazDao
import com.github.raketa92.namazwagtlar.models.NamazTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch


@Database(entities = [NamazTime::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class NamazDB(): RoomDatabase() {
    abstract fun namazDao(): NamazDao

    private class SeedDatabase(private val scope: CoroutineScope): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch { populateDatabase(database.namazDao()) }
            }
        }

        private suspend fun populateDatabase(namazDao: NamazDao) {
//            namazDao.deleteAll()
            // TODO: seed database with sql file
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NamazDB? = null

        @InternalCoroutinesApi
        fun getDatabase(context: Context): NamazDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NamazDB::class.java,
                    "namaz_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}