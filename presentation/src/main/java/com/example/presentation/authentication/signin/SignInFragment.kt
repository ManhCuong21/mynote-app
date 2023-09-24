package com.example.presentation.authentication.signin

import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.onDidEndEditing
import com.example.core.core.viewbinding.viewBinding
import com.example.core.core.lifecycle.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.FragmentSignInBinding
import com.example.presentation.dialog.progress.renderLoadingUI
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : BaseFragment(R.layout.fragment_sign_in) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentSignInBinding by viewBinding()
    override val viewModel: SignInViewModel by viewModels()

    override fun setupViews() {
        setupInitialValues()
        setupClickListener()
        setupEmailInput()
        setupPasswordInput()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is SignInSingleEvent.SignInUser.Success -> {
                            mainNavigator.popBackStack()
                        }

                        is SignInSingleEvent.SignInUser.Failed -> {
                            showTextDialog {
                                textTitle("Sign in error")
                                textContent(event.error.message.toString())
                                negativeButtonAction("OK") {}
                            }
                        }

                        is SignInSingleEvent.ValidationError.Email -> {
                            val errors = event.validationErrors()
                            binding.edtEmail.error = when {
                                SignInUiState.SignInValidationError.EMPTY_EMAIL in errors -> R.string.hint_email_empty_error
                                SignInUiState.SignInValidationError.INVALID_LENGTH_EMAIL in errors -> R.string.hint_email_length_error
                                SignInUiState.SignInValidationError.INVALID_FORMAT_EMAIL in errors -> R.string.hint_email_format_error
                                else -> R.string.hint_null_error
                            }.let { getString(it) }
                        }

                        is SignInSingleEvent.ValidationError.Password -> {
                            val errors = event.validationErrors()
                            binding.edtPassword.error = when {
                                SignInUiState.SignInValidationError.EMPTY_PASSWORD in errors -> R.string.hint_password_empty_error
                                SignInUiState.SignInValidationError.INVALID_LENGTH_PASSWORD in errors -> R.string.hint_password_length_error
                                else -> R.string.hint_null_error
                            }.let { getString(it) }
                        }
                    }
                }
                viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
                    renderLoadingUI(state.isLoading)
                    binding.btnSignIn.isEnabled =
                        state.validationErrors.isEmpty() && state.isActiveButton
                }
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        btnSignIn.setOnClickListener {
            viewModel.dispatch(SignInAction.SignIn)
        }
        btnSignUp.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.SignInFragmentToSignUpFragment)
        }
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }

    private fun setupInitialValues() {
        val state = viewModel.stateFlow.value
        binding.edtEmail.editText!!.setText(state.email)
        binding.edtPassword.editText!!.setText(state.password)
    }

    private fun setupEmailInput() = binding.edtEmail.run {
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    SignInAction.EmailChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
            onDidEndEditing {
                viewModel.dispatch(SignInAction.EmailFocusChanged())
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.edtEmail.error = null
                } else {
                    viewModel.dispatch(SignInAction.EmailFocusChanged())
                }
            }
        }
    }

    private fun setupPasswordInput() = binding.edtPassword.run {
        endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    SignInAction.PasswordChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
            onDidEndEditing {
                viewModel.dispatch(SignInAction.PasswordFocusChanged())
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.edtPassword.error = null
                } else {
                    viewModel.dispatch(SignInAction.PasswordFocusChanged())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arrayOf(
            SignInAction.EmailFocusChanged(force = false),
            SignInAction.PasswordFocusChanged(force = false),
        ).forEach(viewModel::dispatch)
    }
}