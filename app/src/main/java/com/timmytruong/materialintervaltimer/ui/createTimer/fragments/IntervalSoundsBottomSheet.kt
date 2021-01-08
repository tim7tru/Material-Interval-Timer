package com.timmytruong.materialintervaltimer.ui.createTimer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.timmytruong.materialintervaltimer.R
import com.timmytruong.materialintervaltimer.databinding.FragmentIntervalSoundsBottomSheetBinding
import com.timmytruong.materialintervaltimer.ui.createTimer.adapters.IntervalSoundsAdapter
import com.timmytruong.materialintervaltimer.ui.interfaces.OnClickListeners
import com.timmytruong.materialintervaltimer.ui.createTimer.CreateTimerViewModel
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class IntervalSoundsBottomSheet @Inject constructor(
    private val intervalSoundsAdapter: IntervalSoundsAdapter
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentIntervalSoundsBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_interval_sounds_bottom_sheet,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentIntervalsSoundsBottomSheetRecycler.adapter = intervalSoundsAdapter
    }
}