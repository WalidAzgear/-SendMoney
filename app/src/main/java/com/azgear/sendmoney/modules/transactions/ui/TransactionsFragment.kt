package com.azgear.sendmoney.modules.transactions.ui

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.core.extensions.collectLatestLifecycleFlow
import com.azgear.sendmoney.core.extensions.gone
import com.azgear.sendmoney.core.extensions.visible
import com.azgear.sendmoney.core.utils.onError
import com.azgear.sendmoney.core.utils.onLoading
import com.azgear.sendmoney.core.utils.onSuccess
import com.azgear.sendmoney.databinding.FragmentTransactionsBinding
import com.azgear.sendmoney.modules.transactions.viewmodel.TransactionsViewModel

class TransactionsFragment : BaseFragment<FragmentTransactionsBinding, TransactionsViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_transactions
    override val viewModel: TransactionsViewModel by viewModels()
    
    private lateinit var savedRequestsAdapter: SavedRequestsAdapter
    
    override fun setupUI() {
        setupRecyclerView()
        
        // Update title to reflect saved requests
        binding.tvTitle?.text = getString(R.string.saved_requests_title)
        
        // Pull to refresh
        binding.swipeRefresh?.setOnRefreshListener {
            viewModel.refreshSavedRequests()
        }
    }
    
    private fun setupRecyclerView() {
        savedRequestsAdapter = SavedRequestsAdapter { request ->
            showRequestDetails(request.id)
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = savedRequestsAdapter
        }
    }
    
    private fun showRequestDetails(requestId: String) {
        val dialog = RequestDetailsDialogFragment.newInstance(requestId)
        dialog.show(parentFragmentManager, "RequestDetailsDialog")
    }
    
    override fun observeViewModel() {
        collectLatestLifecycleFlow(viewModel.savedRequestsState) { state ->
            state
                .onLoading {
                    binding.swipeRefresh?.isRefreshing = true
                }
                .onSuccess { response ->
                    binding.swipeRefresh?.isRefreshing = false
                    
                    if (response.requests.isEmpty()) {
                        binding.rvTransactions.gone()
                        binding.tvEmptyState?.visible()
                        binding.tvEmptyState?.text = "No saved requests found"
                    } else {
                        binding.rvTransactions.visible()
                        binding.tvEmptyState?.gone()
                        savedRequestsAdapter.submitList(response.requests)
                    }
                }
                .onError { message, _ ->
                    binding.swipeRefresh?.isRefreshing = false
                    binding.rvTransactions.gone()
                    binding.tvEmptyState?.visible()
                    binding.tvEmptyState?.text = "Error loading saved requests"
                }
        }
    }
}