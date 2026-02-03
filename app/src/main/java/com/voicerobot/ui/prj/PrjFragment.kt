package com.voicerobot.ui.prj

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.voicerobot.R
import com.voicerobot.databinding.FragmentPrjBinding

class PrjFragment : Fragment(R.layout.fragment_prj) {

    private var _binding: FragmentPrjBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrjBinding.bind(view)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

