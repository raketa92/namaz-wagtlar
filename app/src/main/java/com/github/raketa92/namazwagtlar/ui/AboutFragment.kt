package com.github.raketa92.namazwagtlar.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.raketa92.namazwagtlar.R
import com.github.raketa92.namazwagtlar.databinding.FragmentAboutBinding
import com.github.raketa92.namazwagtlar.viewmodel.SharedViewModel

class AboutFragment : Fragment() {

    private var binding: FragmentAboutBinding? = null

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }
}