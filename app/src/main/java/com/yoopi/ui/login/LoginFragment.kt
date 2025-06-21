package com.yoopi.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible                // <<< new
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.yoopi.player.R
import com.yoopi.player.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch                  // coroutine builder
import android.content.Intent
import com.yoopi.ui.channels.ChannelsActivity   // <-- add this line
import androidx.fragment.app.activityViewModels



@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _bind: FragmentLoginBinding? = null
    private val bind get() = _bind!!

    private val vm: LoginViewModel by activityViewModels()

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = FragmentLoginBinding.inflate(i, c, false).also { _bind = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        /* pager & tabs */
        bind.viewPager.adapter = LoginPagerAdapter(this)
        TabLayoutMediator(bind.tabLayout, bind.viewPager) { tab, pos ->
            tab.text = if (pos == 0) "Xtream" else "M3U"
        }.attach()

        /* ─────── one-shot events ─────── */

        vm.goToChannels.observe(viewLifecycleOwner) { playlistId ->
            val intent = Intent(requireContext(), ChannelsActivity::class.java)
                .putExtra("playlistId", playlistId)

            startActivity(intent)
            // optional: close LoginActivity so BACK doesn’t return here
            requireActivity().finish()
        }


        /* ─────── optional spinner ─────── */

        vm.state.observe(viewLifecycleOwner) { state ->
            bind.progress.isVisible = state is LoginViewModel.LoadState.Loading
        }
    }

    override fun onDestroyView() { _bind = null; super.onDestroyView() }
}
