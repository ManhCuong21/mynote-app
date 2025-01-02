package com.example.presentation.main.setting.security.changeunlockcode

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener

class PasswordOTPView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val numOfFields = 6  // Số lượng ô nhập OTP
    private val editTexts = mutableListOf<EditText>()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setupFields()
    }

    private fun setupFields() {
        for (i in 0 until numOfFields) {
            val editText = EditText(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                gravity = Gravity.CENTER
                textSize = 18f
                maxLines = 1
                isFocusable = true
                isFocusableInTouchMode = true
                isCursorVisible = true
                setTextColor(Color.BLACK)
                setHintTextColor(Color.GRAY)
                filters = arrayOf(android.text.InputFilter.LengthFilter(1)) // Chỉ cho phép 1 ký tự
            }

            // Lắng nghe sự thay đổi văn bản và tự động chuyển ô khi người dùng nhập
            editText.addTextChangedListener {
                if (it?.length == 1 && i < numOfFields - 1) {
                    // Chuyển đến ô tiếp theo
                    editTexts[i + 1].requestFocus()
                } else if (it.isNullOrEmpty() && i > 0) {
                    // Chuyển về ô trước nếu người dùng xóa ký tự
                    editTexts[i - 1].requestFocus()
                }
            }

            addView(editText)
            editTexts.add(editText)
        }
    }

    // Lấy mã OTP đã nhập
    fun getOTP(): String {
        return editTexts.joinToString("") { it.text.toString() }
    }
}