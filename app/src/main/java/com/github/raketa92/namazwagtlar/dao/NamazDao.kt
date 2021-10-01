package com.github.raketa92.namazwagtlar.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.raketa92.namazwagtlar.models.NamazTime
import java.time.LocalDate

@Dao
interface NamazDao {

    @Query("SELECT * FROM namazTime ORDER BY id ASC")
    fun getAllData(): LiveData<List<NamazTime>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(namazTime: NamazTime)

    @Query("SELECT * FROM namazTime WHERE date = :date")
    @Throws(Exception::class)
    fun getByDate(date: LocalDate): LiveData<NamazTime>

    @Query("SELECT * FROM namazTime WHERE date = :date")
    @Throws(Exception::class)
    fun getTimesByDate(date: LocalDate): NamazTime

    @Query("DELETE FROM namazTime")
    suspend fun deleteAll()

}