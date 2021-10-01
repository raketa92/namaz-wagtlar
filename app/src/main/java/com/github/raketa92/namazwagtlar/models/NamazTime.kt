package com.github.raketa92.namazwagtlar.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Entity(tableName = "namazTime")
@Parcelize
data class NamazTime(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: LocalDate,
    var fasting: String,
    var fajr: String,
    var sunrise: String,
    var zuhr: String,
    var asr: String,
    var magrib: String,
    var isha: String
    ): Parcelable