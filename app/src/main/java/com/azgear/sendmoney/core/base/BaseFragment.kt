package com.azgear.sendmoney.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment<T : ViewDataBinding, V : ViewModel> : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding!!

    protected abstract val layoutId: Int
    protected abstract val viewModel: V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupUI()
        observeViewModel()
    }

    protected abstract fun setupUI()
    protected abstract fun observeViewModel()

    /**
     * Helper function to collect StateFlow/Flow with lifecycle awareness
     */
    protected fun <T> collectLatestLifecycleFlow(
        flow: Flow<T>,
        collect: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(collect)
            }
        }
    }

    /**
     * Helper function to collect Flow with lifecycle awareness
     */
    protected fun <T> collectLifecycleFlow(
        flow: Flow<T>,
        collect: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(collect)
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    protected fun collectState(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            block()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}