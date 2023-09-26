package com.example.presentation.authentication.information

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentUserInformationBinding
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserInformationFragment : BaseFragment(R.layout.fragment_user_information) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentUserInformationBinding by viewBinding()
    override val viewModel: UserInformationViewModel by viewModels()

    override fun setupViews() {
        setupText()
        setupClickListener()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is UserInformationSingleEvent.DeleteUser.Success -> {
                            sharedPrefersManager.userEmail = null
                            mainNavigator.popBackStack()
                        }

                        is UserInformationSingleEvent.DeleteUser.Failed -> {
                            showTextDialog {
                                textTitle("Delete user error")
                                textContent(event.error.message.toString())
                                negativeButtonAction("OK") {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupText() = binding.apply {
        tvEmail.text = sharedPrefersManager.userEmail
    }

    private fun setupClickListener() = binding.apply {
        btnDeleteUser.setOnClickListener {
            showTextDialog {
                textTitle("Confirm")
                textContent(
                    "Are you sure you want to delete this account? " +
                            "\n(This action cannot be reversed)"
                )
                positiveButtonAction("Ok") {
                    viewModel.dispatch(UserInformationAction.DeleteUser)
                }
                negativeButtonAction("Cancel") {}
            }
        }
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }
}