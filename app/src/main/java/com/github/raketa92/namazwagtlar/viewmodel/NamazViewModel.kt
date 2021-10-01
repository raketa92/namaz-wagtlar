package com.github.raketa92.namazwagtlar.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.github.raketa92.namazwagtlar.db.NamazDB
import com.github.raketa92.namazwagtlar.models.NamazTime
import com.github.raketa92.namazwagtlar.repository.NamazRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@InternalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.O)
class NamazViewModel(application: Application): AndroidViewModel(application) {

    @InternalCoroutinesApi
    private val namazDao = NamazDB.getDatabase(application).namazDao()
    @InternalCoroutinesApi
    private val repository: NamazRepo = NamazRepo(namazDao)
    var todayTimes: LiveData<NamazTime>
//    var selectedDate: LocalDate? = null
    init {
        todayTimes = repository.getByDate(currentDate())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun currentDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(LocalDate.now().toString(), formatter)
    }

    @InternalCoroutinesApi
    suspend fun insertData(namazTime: NamazTime) {
//        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(namazTime)
//        }
    }

    fun getByDate(date: LocalDate): LiveData<NamazTime> {
        return repository.getByDate(date)
    }

}