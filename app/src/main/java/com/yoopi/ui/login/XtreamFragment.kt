package com.yoopi.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yoopi.player.databinding.FragmentXtreamBinding    // 👈 correct package

class XtreamFragment : Fragment() {

    private var _binding: FragmentXtreamBinding? = null
    private val binding get() = _binding!!
    private val vm: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXtreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        saveXtreamBtn.setOnClickListener {
            vm.saveXtream(
                serverUrl.text.toString(),
                username.text.toString(),
                password.text.toString(),
                requireContext()
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
