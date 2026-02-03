package com.voicerobot.ui.share

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.voicerobot.R

class ScreenShareFragment : Fragment(R.layout.fragment_screen_share) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AlertDialog.Builder(requireContext())
            .setTitle("共享屏幕")
            .setMessage("当前版本暂未接入屏幕共享功能")
            .setCancelable(false)
            .setPositiveButton("确定") { d, _ ->
                d.dismiss()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .show()
    }
}

