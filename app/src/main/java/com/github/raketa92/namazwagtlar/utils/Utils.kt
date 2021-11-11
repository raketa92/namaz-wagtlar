package com.github.raketa92.namazwagtlar.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.raketa92.namazwagtlar.models.NamazTime
import com.github.raketa92.namazwagtlar.models.Time
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun parseCsv(fileStream: InputStream): List<NamazTime> {
    val COL_DATE = 0
    val COL_FASTING = 1
    val COL_FAJR = 2
    val COL_SUNRISE = 3
    val COL_ZUHR = 4
    val COL_ASR = 5
    val COL_MAGRIB = 6
    val COL_ISHA = 7
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val results = mutableListOf<NamazTime>()

    csvReader().open(fileStream) {
        readAllAsSequence().forEach { row ->
            val tokens: List<String> = row[0].split(";")
            if (tokens.size != 8) {
                Log.w("CSVParser", "Skipping bad csv row")
                return@forEach
            }
            if (tokens[COL_DATE] == "date") return@forEach

            val namazTime = NamazTime(
                id = 0,
                date = LocalDate.parse(tokens[COL_DATE], formatter),
                fasting = tokens[COL_FASTING],
                fajr = tokens[COL_FAJR],
                sunrise = tokens[COL_SUNRISE],
                zuhr = tokens[COL_ZUHR],
                asr = tokens[COL_ASR],
                magrib = tokens[COL_MAGRIB],
                isha = tokens[COL_ISHA]
            )
            results.add(namazTime)
        }
    }

    fileStream.close()
    return results
}

fun getTodayHours(namazTime: NamazTime): List<Time> {
    val hours = mutableListOf<Time>()
    hours.add(Time(namazTime.fajr.split(":")[0].toInt(), namazTime.fajr.split(":")[1].toInt(), "fajr"))
    hours.add(Time(namazTime.zuhr.split(":")[0].toInt(), namazTime.zuhr.split(":")[1].toInt(), "zuhr"))
    hours.add(Time(namazTime.asr.split(":")[0].toInt(), namazTime.asr.split(":")[1].toInt(), "asr"))
    hours.add(Time(namazTime.magrib.split(":")[0].toInt(), namazTime.magrib.split(":")[1].toInt(), "magrib"))
    hours.add(Time(namazTime.isha.split(":")[0].toInt(), namazTime.isha.split(":")[1].toInt(), "isha"))
    return hours
}