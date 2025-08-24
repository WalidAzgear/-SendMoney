package com.azgear.sendmoney.modules.transactions.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.extensions.collectLatestLifecycleFlow
import com.azgear.sendmoney.core.utils.onError
import com.azgear.sendmoney.core.utils.onLoading
import com.azgear.sendmoney.core.utils.onSuccess
import com.azgear.sendmoney.databinding.DialogRequestDetailsBinding
import com.azgear.sendmoney.modules.transactions.viewmodel.TransactionsViewModel
import com.google.android.material.snackbar.Snackbar

class RequestDetailsDialogFragment : DialogFragment() {
    
    private lateinit var binding: DialogRequestDetailsBinding
    private val viewModel: TransactionsViewModel by viewModels()
    
    companion object {
        private const val ARG_REQUEST_ID = "request_id"
        
        fun newInstance(requestId: String): RequestDetailsDialogFragment {
            val fragment = RequestDetailsDialogFragment()
            val args = Bundle()
            args.putString(ARG_REQUEST_ID, requestId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_request_details, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        loadRequestDetails()
    }
    
    override fun onStart() {
        super.onStart()
        
        // Make dialog take up most of the screen
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
    }
    
    private fun setupUI() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        binding.copyButton.setOnClickListener {
            copyJsonToClipboard()
        }
    }
    
    private fun observeViewModel() {
        collectLatestLifecycleFlow(viewModel.selectedRequestState) { state ->
            state
                .onLoading {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.jsonContainer.visibility = View.GONE
                }
                .onSuccess { request ->
                    binding.progressBar.visibility = View.GONE
                    binding.jsonContainer.visibility = View.VISIBLE
                    
                    binding.requestIdTitle.text = getString(R.string.request_details_title) + " - ${request.id}"
                    binding.jsonTextView.text = request.toFormattedJson()
                }
                .onError { message, _ ->
                    binding.progressBar.visibility = View.GONE
                    binding.jsonContainer.visibility = View.GONE
                    showErrorMessage(message)
                }
        }
    }
    
    private fun loadRequestDetails() {
        val requestId = arguments?.getString(ARG_REQUEST_ID)
        if (requestId != null) {
            viewModel.getRequestDetails(requestId)
        } else {
            showErrorMessage("Request ID not provided")
            dismiss()
        }
    }
    
    private fun copyJsonToClipboard() {
        val jsonText = binding.jsonTextView.text.toString()
        if (jsonText.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) 
                as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Request JSON", jsonText)
            clipboard.setPrimaryClip(clip)
            
            Snackbar.make(binding.root, "JSON copied to clipboard", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelectedRequest()
    }
} 