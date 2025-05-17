package com.yoopi.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yoopi.R
import dagger.hilt.android.AndroidEntryPoint
import by.kirich1409.viewbindingdelegate.viewBinding
import com.yoopi.databinding.FragmentLoginBinding

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        saveXtreamBtn.setOnClickListener {
            viewModel.saveXtream(
                serverUrl.text.toString(),
                username.text.toString(),
                password.text.toString()
            )
        }
        saveM3uBtn.setOnClickListener {
            viewModel.saveM3u(m3uUrl.text.toString())
        }
    }
}