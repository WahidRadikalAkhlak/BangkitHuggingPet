package com.bangkit.huggingpet.customeview

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.bangkit.huggingpet.R

class IsPasswordSameCV : AppCompatEditText, View.OnFocusChangeListener,
    View.OnTouchListener {

    var isPasswordSameValid = false
    var isPasswordVisible: Boolean = false
    var passwordCustomeView = PasswordCV(context)


    init {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

//    constructor(context: Context, passwordCustomeView: PasswordCustomeView) : super(context) {
//        this.passwordCustomeView = passwordCustomeView
//        init()
//    }

    private fun init() {
        transformationMethod = PasswordTransformationMethod.getInstance()

        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && event.rawX >= (right - compoundDrawables[2].bounds.width())) {
            togglePasswordVisibility()
            return true
        }
        return false
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        transformationMethod = if (isPasswordVisible) {
            null // Show the password
        } else {
            PasswordTransformationMethod.getInstance() // Hide the password
        }

        // Set the cursor position to the end
        text?.let { setSelection(it.length) }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) {
            validatePassword()
        }
    }

    private fun validatePassword() {
        val password = text.toString().trim()
        val confirmPassword = text.toString().trim()

        isPasswordSameValid = password == confirmPassword && password.length >= 8


        error = if (!isPasswordSameValid) {
            resources.getString(R.string.passwordNotMatch)
        } else {
            null
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (!hasFocus) {
            validatePassword()
        }
    }
}
