package com.github.raketa92.namazwagtlar.ui

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.github.raketa92.namazwagtlar.databinding.FragmentMainBinding
import com.github.raketa92.namazwagtlar.models.NamazTime
import com.github.raketa92.namazwagtlar.viewmodel.NamazViewModel
import com.github.raketa92.namazwagtlar.viewmodel.SharedViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.times_layout.*
import kotlinx.coroutines.InternalCoroutinesApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainFragment : Fragment() {
    private val PREF_DARK_MODE_ENABLE = "enable_dark_mode"

    private var selectedDate: LocalDate? = null
    private var calendar: Calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private var binding: FragmentMainBinding? = null

    @InternalCoroutinesApi
    private val namazViewModel: NamazViewModel by viewModels ()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val theme = prefs.getBoolean(PREF_DARK_MODE_ENABLE, false)
        if (theme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        sharedViewModel.selectedDate?.observe(viewLifecycleOwner, {
            selectedDate = it
        })
        setupDatePicker()
        updateView()
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDatePicker() {
        if (sharedViewModel.selectedDate == null) {
            val today = sdf.format(calendar.time)
            selectedDate = LocalDate.parse(today, formatter)
            sharedViewModel.selectedDate?.value = selectedDate
        }

        val dateListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateView()
            }

        binding?.dateTV?.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    @InternalCoroutinesApi
    private fun setData(namazTime: NamazTime) {
        fastingTime.text = namazTime.fasting
        fajrTime.text = namazTime.fajr
        sunriseTime.text = namazTime.sunrise
        zuhrTime.text = namazTime.zuhr
        asrTime.text = namazTime.asr
        magribTime.text = namazTime.magrib
        ishaTime.text = namazTime.isha
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @InternalCoroutinesApi
    private fun updateView() {
        dateTV.text = sdf.format(calendar.time)
        selectedDate = LocalDate.parse(dateTV.text, formatter)
        sharedViewModel.selectedDate?.value = selectedDate
        selectedDate?.let { date ->
            //TODO: create date with 2021 year
            val todayTime = LocalDateTime.of(2021, date.month, date.dayOfMonth, 0, 0, 0,)
            val today = todayTime.toLocalDate()
            namazViewModel.getByDate(today).observe(viewLifecycleOwner) { namazTime ->
                setData(namazTime)
            }
        }
    }
}