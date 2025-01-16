package com.example.presentation.main.setting.security.changeunlockcode

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.presentation.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
class PasswordOTPView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val numOfFields = 6
    private val editTexts = mutableListOf<EditText>()
    private var otpCompleteListener: OtpCompleteListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setupFields()
    }

    fun setOtpCompleteListener(listener: OtpCompleteListener) {
        otpCompleteListener = listener
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupFields() {
        for (i in 0 until numOfFields) {
            val editText = EditText(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(24, 0, 24, 0)
                }
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
                background = ContextCompat.getDrawable(context, R.drawable.underline_color)
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                textSize = 50f
                maxLines = 1
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = false
                setTextColor(Color.BLACK)
                setHintTextColor(Color.GRAY)
                filters = arrayOf(android.text.InputFilter.LengthFilter(1))
                setPadding(16, 8, 16, 0)
                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            }

            editText.addTextChangedListener { text ->
                handleTextChanged(i, text?.length == 1)
            }

            // Lắng nghe sự kiện phím để xử lý khi nhấn "xóa"
            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Kiểm tra nếu ô hiện tại có ký tự thì xóa ký tự đó
                    if (editTexts[i].text.isNotEmpty()) {
                        editTexts[i].setText("")
                    } else if (i > 0) {
                        // Nếu ô hiện tại trống và có ô trước, xóa ký tự ở ô trước
                        val previousEditText = editTexts[i - 1]
                        if (previousEditText.text.isNotEmpty()) {
                            previousEditText.setText("")
                            previousEditText.requestFocus()
                        }
                    }
                    return@setOnKeyListener true
                }
                false
            }

            addView(editText)
            editTexts.add(editText)
        }
        requestFocusAndShowKeyboard()
    }

    private fun requestFocusAndShowKeyboard() {
        editTexts[0].requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        MainScope().launch {
            delay(100)
            imm.showSoftInput(editTexts[0], InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun handleTextChanged(index: Int, isFilled: Boolean) {
        if (isFilled && index < numOfFields - 1) {
            editTexts[index + 1].requestFocus()
        }
        if (index == numOfFields - 1 && isFilled) {
            otpCompleteListener?.onOtpComplete(getOTP())
        }
    }

    private fun getOTP(): String {
        return editTexts.joinToString("") { it.text.toString() }
    }

    interface OtpCompleteListener {
        fun onOtpComplete(otp: String)
    }
}