package com.yoopi.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yoopi.player.databinding.FragmentM3uBinding       // 👈 correct package

class M3uFragment : Fragment() {

    private var _binding: FragmentM3uBinding? = null
    private val binding get() = _binding!!
    private val vm: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentM3uBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        saveM3uBtn.setOnClickListener {
            vm.saveM3u(
                m3uUrl.text.toString(),
                requireContext()
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
