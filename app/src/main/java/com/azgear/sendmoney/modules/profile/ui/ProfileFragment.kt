package com.azgear.sendmoney.modules.profile.ui

import androidx.fragment.app.viewModels
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.databinding.FragmentProfileBinding
import com.azgear.sendmoney.modules.profile.viewmodel.ProfileViewModel

class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_profile
    override val viewModel: ProfileViewModel by viewModels()
    
    override fun setupUI() {
        binding.viewModel = viewModel
    }
    
    override fun observeViewModel() {
    }
} 