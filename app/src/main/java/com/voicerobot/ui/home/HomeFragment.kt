package com.voicerobot.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.voicerobot.R
import com.voicerobot.databinding.FragmentHomeBinding
import com.voicerobot.lottie.AgentAnimMapper
import com.voicerobot.ui.chat.ChatAdapter
import com.voicerobot.ui.robot.MainViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: MainViewModel by activityViewModels()

    private val chatAdapter = ChatAdapter()
    private var isChatVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.rvConversation.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConversation.adapter = chatAdapter

        val toggle = requireActivity().findViewById<android.widget.TextView>(R.id.tvToggleTextPanel)
        toggle.setOnClickListener {
            isChatVisible = !isChatVisible
            binding.rvConversation.visibility = if (isChatVisible) View.VISIBLE else View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    val res = AgentAnimMapper.animRes(state.phase)
                    binding.robotAvatar.setAnimation(res)
                    binding.robotAvatar.playAnimation()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.chatMessages.collect { list ->
                    chatAdapter.submitList(list)
                    if (list.isNotEmpty()) {
                        binding.rvConversation.post {
                            binding.rvConversation.smoothScrollToPosition(list.lastIndex)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
