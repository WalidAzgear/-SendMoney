package com.azgear.sendmoney.modules.confirmation.ui

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.core.utils.onError
import com.azgear.sendmoney.core.utils.onLoading
import com.azgear.sendmoney.core.utils.onSuccess
import com.azgear.sendmoney.databinding.FragmentConfirmationBinding
import com.azgear.sendmoney.modules.confirmation.viewmodel.ConfirmationViewModel

class ConfirmationFragment : BaseFragment<FragmentConfirmationBinding, ConfirmationViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_confirmation
    override val viewModel: ConfirmationViewModel by viewModels()
    
    private val args: ConfirmationFragmentArgs by navArgs()
    
    override fun setupUI() {
        // Set transaction data from arguments
        viewModel.setTransactionData(
            args.recipientName,
            args.amount.toDouble(),
            args.note
        )
        
        binding.btnConfirm.setOnClickListener {
            viewModel.confirmTransaction()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }
    
    override fun observeViewModel() {
        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            binding.tvRecipient.text = state.recipientName
            binding.tvAmount.text = state.formattedAmount
            
            if (state.hasNote) {
                binding.tvNote.text = state.note
                binding.tvNoteLabel.visibility = android.view.View.VISIBLE
                binding.tvNote.visibility = android.view.View.VISIBLE
            } else {
                binding.tvNoteLabel.visibility = android.view.View.GONE
                binding.tvNote.visibility = android.view.View.GONE
            }
        }
        
        collectLatestLifecycleFlow(viewModel.confirmationState) { state ->
            state
                .onLoading {
                    binding.btnConfirm.isEnabled = false
                    binding.btnConfirm.text = "Processing..."
                }
                .onSuccess { confirmation ->
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = "Confirm & Send"
                    
                    // Navigate back to home and show success message
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
                .onError { message, _ ->
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = "Confirm & Send"
                    
                    // Show error message (you can implement snackbar here)
                }
        }
    }
}