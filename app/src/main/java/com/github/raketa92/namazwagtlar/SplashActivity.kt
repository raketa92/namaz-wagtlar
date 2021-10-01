package com.github.raketa92.namazwagtlar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.raketa92.namazwagtlar.utils.parseCsv
import com.github.raketa92.namazwagtlar.viewmodel.NamazViewModel
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.IOException


class SplashActivity : AppCompatActivity() {
    private val PREF_NAME = "com.github.raketa92.namazwagtlar"
    private val PREF_VERSION_CODE = "version_code"
    private val DOESNT_EXIST = -1

    @InternalCoroutinesApi
    private val namazViewModel: NamazViewModel by viewModels ()
    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            checkFirstRun()
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkFirstRun() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val savedVersionCode = prefs?.getInt(PREF_VERSION_CODE, DOESNT_EXIST)
        when {
            savedVersionCode == currentVersionCode -> {
                // normal start
                Log.d("debug", "normal start")
                return
            }
            savedVersionCode == DOESNT_EXIST -> {
                // first start
                Log.d("debug", "first start")
                seedDB()
            }
            currentVersionCode > savedVersionCode!! -> {
                // updated start
                Log.d("debug", "update start")
            }
        }
        prefs.edit().putInt(PREF_VERSION_CODE, currentVersionCode).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @InternalCoroutinesApi
    private fun seedDB() {
        var i = 0
        Log.d("debug","Seeding DB...")
        try {
            val fileStream = assets?.open("db/times.csv")
            val timesFromCsv = fileStream?.let { parseCsv(it) }
            if (timesFromCsv != null) {
                timesFromCsv.forEach { row ->
                    runBlocking(Dispatchers.IO){
                        namazViewModel.insertData(row)
                        Log.d("debug", "inserting item: $i")
                        i++
                    }
                }
                Log.d("debug", "done inserting")
            } else {
                Log.d("debug", "no data parsed")
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}