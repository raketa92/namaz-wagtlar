package com.github.raketa92.namazwagtlar.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.github.raketa92.namazwagtlar.dao.NamazDao
import com.github.raketa92.namazwagtlar.models.NamazTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class NamazRepo @Inject constructor(private val namazDao: NamazDao) {

    val getAllData: LiveData<List<NamazTime>> = namazDao.getAllData()

    suspend fun insertData(namazTime: NamazTime) {
        namazDao.insertData(namazTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getByDate(date: LocalDate): LiveData<NamazTime> {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return namazDao.getByDate(LocalDate.parse(date.toString(), formatter))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getByDateList(date: LocalDate): NamazTime {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return namazDao.getTimesByDate(LocalDate.parse(date.toString(), formatter))
    }

}