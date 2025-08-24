package com.azgear.sendmoney.modules.home.ui

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.core.utils.onError
import com.azgear.sendmoney.core.utils.onLoading
import com.azgear.sendmoney.core.utils.onSuccess
import com.azgear.sendmoney.databinding.FragmentHomeBinding
import com.azgear.sendmoney.modules.home.viewmodel.HomeViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()
    
    override fun setupUI() {
        Log.d("HomeFragment", "setupUI() called")
        
        // Send Money card click
        binding.cardSendMoney.setOnClickListener {
            Log.d("HomeFragment", "Send Money card clicked")
            findNavController().navigate(R.id.action_home_to_sendMoney)
        }
        
        // View Saved Requests card click
        binding.cardViewTransactions.setOnClickListener {
            Log.d("HomeFragment", "View Transactions card clicked")
            findNavController().navigate(R.id.action_home_to_transactions)
        }
        
        // Balance card actions
        binding.btnAddMoney.setOnClickListener {
            Log.d("HomeFragment", "Add Money button clicked")
            // TODO: Implement add money functionality
        }
        
        binding.btnHistory.setOnClickListener {
            Log.d("HomeFragment", "History button clicked")
            findNavController().navigate(R.id.action_home_to_transactions)
        }
        
        // Notification click
        binding.ivNotification.setOnClickListener {
            Log.d("HomeFragment", "Notification icon clicked")
            // TODO: Implement notifications
        }
        
        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            Log.d("HomeFragment", "Pull to refresh triggered")
            viewModel.refreshBalance()
        }
        
        Log.d("HomeFragment", "setupUI() completed - UI elements bound")
    }
    
    override fun observeViewModel() {
        Log.d("HomeFragment", "observeViewModel() called")
        
        // Trigger initial balance load
        viewModel.loadBalance()
        
        collectLatestLifecycleFlow(viewModel.balanceState) { state ->
            Log.d("HomeFragment", "Balance state received: $state")
            state
                .onLoading {
                    Log.d("HomeFragment", "Balance loading...")
                    binding.swipeRefresh.isRefreshing = true
                }
                .onSuccess { balanceResponse ->
                    Log.d("HomeFragment", "Balance loaded: ${balanceResponse.formattedBalance}")
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvBalance.text = balanceResponse.formattedBalance
                }
                .onError { message, _ ->
                    Log.e("HomeFragment", "Balance error: $message")
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvBalance.text = "Error loading balance"
                    // You can show a snackbar or toast here
                }
        }
        
        Log.d("HomeFragment", "observeViewModel() completed")
    }
}