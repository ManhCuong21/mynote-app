package com.example.presentation.authentication.signup

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
import com.example.presentation.databinding.FragmentSignUpBinding
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : BaseFragment(R.layout.fragment_sign_up) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentSignUpBinding by viewBinding()
    override val viewModel: SignUpViewModel by viewModels()

    override fun setupViews() {
        setupInitialValues()
        setupClickListener()
        setupEmailInput()
        setupPasswordInput()
        setupPasswordConfirmInput()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is SignUpSingleEvent.SignUpUser.Success -> {

                        }

                        is SignUpSingleEvent.SignUpUser.Failed -> {
                            showTextDialog {
                                textTitle("Sign up error")
                                textContent(event.error.message.toString())
                                negativeButtonAction("OK") {}
                            }
                        }

                        is SignUpSingleEvent.ValidationError.Email -> {
                            val errors = event.validationErrors()
                            binding.edtEmail.error = when {
                                SignUpUiState.SignUpValidationError.EMPTY_EMAIL in errors -> R.string.hint_email_empty_error
                                SignUpUiState.SignUpValidationError.INVALID_LENGTH_EMAIL in errors -> R.string.hint_email_length_error
                                SignUpUiState.SignUpValidationError.INVALID_FORMAT_EMAIL in errors -> R.string.hint_email_format_error
                                else -> R.string.hint_null_error
                            }.let { getString(it) }
                        }

                        is SignUpSingleEvent.ValidationError.Password -> {
                            val errors = event.validationErrors()
                            binding.edtPassword.error = when {
                                SignUpUiState.SignUpValidationError.EMPTY_PASSWORD in errors -> R.string.hint_password_empty_error
                                SignUpUiState.SignUpValidationError.INVALID_LENGTH_PASSWORD in errors -> R.string.hint_password_length_error
                                else -> R.string.hint_null_error
                            }.let { getString(it) }
                        }

                        is SignUpSingleEvent.ValidationError.PasswordConfirm -> {
                            val errors = event.validationErrors()
                            binding.edtPasswordConfirm.error = when {
                                SignUpUiState.SignUpValidationError.EMPTY_PASSWORD_CONFIRM in errors -> R.string.hint_password_empty_error
                                SignUpUiState.SignUpValidationError.INVALID_LENGTH_PASSWORD_CONFIRM in errors -> R.string.hint_password_length_error
                                SignUpUiState.SignUpValidationError.NOT_MATCH_PASSWORD_CONFIRM in errors -> R.string.hint_password_confirm_not_match_error
                                else -> R.string.hint_null_error
                            }.let { getString(it) }
                        }
                    }
                }
                viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
                    binding.btnSignUp.isEnabled =
                        state.validationErrors.isEmpty() && state.isActiveButton
                }
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        btnSignUp.setOnClickListener {
            viewModel.dispatch(SignUpAction.SignUp)
        }
        btnSignIn.setOnClickListener {
            mainNavigator.popBackStack()
        }
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }

    private fun setupInitialValues() {
        val state = viewModel.stateFlow.value
        binding.edtEmail.editText!!.setText(state.email)
        binding.edtPassword.editText!!.setText(state.password)
        binding.edtPasswordConfirm.editText!!.setText(state.passwordConfirm)
    }

    private fun setupEmailInput() = binding.edtEmail.run {
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    SignUpAction.EmailChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
            onDidEndEditing {
                viewModel.dispatch(SignUpAction.EmailFocusChanged())
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.edtEmail.error = null
                } else {
                    viewModel.dispatch(SignUpAction.EmailFocusChanged())
                }
            }
        }
    }

    private fun setupPasswordInput() = binding.edtPassword.run {
        endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    SignUpAction.PasswordChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
            onDidEndEditing {
                viewModel.dispatch(SignUpAction.PasswordFocusChanged())
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.edtPassword.error = null
                } else {
                    viewModel.dispatch(SignUpAction.PasswordFocusChanged())
                }
            }
        }
    }

    private fun setupPasswordConfirmInput() = binding.edtPasswordConfirm.run {
        endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        editText?.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(
                    SignUpAction.PasswordConfirmChanged(
                        text?.toString().orEmpty()
                    )
                )
            }
            onDidEndEditing {
                viewModel.dispatch(SignUpAction.PasswordConfirmFocusChanged())
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.edtPasswordConfirm.error = null
                } else {
                    viewModel.dispatch(SignUpAction.PasswordConfirmFocusChanged())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arrayOf(
            SignUpAction.EmailFocusChanged(force = false),
            SignUpAction.PasswordFocusChanged(force = false),
            SignUpAction.PasswordConfirmFocusChanged(force = false)
        ).forEach(viewModel::dispatch)
    }
}