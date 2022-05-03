package com.nex3z.notificationbadge

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.TextView
import com.nex3z.notificationbadge.databinding.NotificationBadgeBinding

class NotificationBadge(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var animationEnabled: Boolean = DEFAULT_ANIMATION_ENABLED
    var animationDuration: Int = DEFAULT_ANIMATION_DURATION
    var maxTextLength: Int = DEFAULT_MAX_TEXT_LENGTH
    var ellipsizeText: String = DEFAULT_ELLIPSIZE_TEXT

    val binding: NotificationBadgeBinding by lazy {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        NotificationBadgeBinding.inflate(inflater, this, true)
    }

    var textColor: Int
        get() = binding.tvBadgeText.currentTextColor
        set(color) = binding.tvBadgeText.setTextColor(color)

    val textView: TextView
        get() = binding.tvBadgeText

    var badgeBackgroundDrawable: Drawable?
        get() = binding.ivBadgeBg.drawable
        set(drawable) = binding.ivBadgeBg.setImageDrawable(drawable)

    private var isVisible: Boolean
        get() = binding.flContainer.visibility == VISIBLE
        set(value) { binding.flContainer.visibility = if (value) View.VISIBLE else INVISIBLE }

    private val update: Animation by lazy {
        ScaleAnimation(1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = animationDuration.toLong()
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
    }

    private val show: Animation by lazy {
        ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = animationDuration.toLong()
        }
    }

    private val hide: Animation by lazy {
        ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = animationDuration.toLong()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    isVisible = false
                }
                override fun onAnimationStart(animation: Animation?) {}
            })
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.NotificationBadge, 0, 0)
        try {
            val textColor = a.getColor(R.styleable.NotificationBadge_android_textColor,
                    DEFAULT_TEXT_COLOR.toInt())
            binding.tvBadgeText.setTextColor(textColor)

            val textSize = a.getDimension(R.styleable.NotificationBadge_android_textSize,
                    dpToPx(DEFAULT_TEXT_SIZE))
            binding.tvBadgeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

            animationEnabled = a.getBoolean(R.styleable.NotificationBadge_nbAnimationEnabled,
                    DEFAULT_ANIMATION_ENABLED)
            animationDuration = a.getInt(R.styleable.NotificationBadge_nbAnimationDuration,
                    DEFAULT_ANIMATION_DURATION)

            a.getDrawable(R.styleable.NotificationBadge_nbBackground)?.let {
                binding.ivBadgeBg.setImageDrawable(it)
            }

            maxTextLength = a.getInt(R.styleable.NotificationBadge_nbMaxTextLength,
                    DEFAULT_MAX_TEXT_LENGTH)
            a.getString(R.styleable.NotificationBadge_nbEllipsizeText)?.let {
                ellipsizeText = it
            }
        } finally {
            a.recycle()
        }
    }

    @JvmOverloads
    fun setText(text: CharSequence?, animation: Boolean = animationEnabled) {
        val badgeText = when {
            text == null -> String()
            text.length > maxTextLength -> ellipsizeText
            else -> text
        }
        if (badgeText.isEmpty()) {
            clear(animation)
        } else if (animation) {
            if (isVisible) {
                binding.flContainer.startAnimation(update)
            } else {
                binding.flContainer.startAnimation(show)
            }
        }
        binding.tvBadgeText.text = badgeText
        isVisible = true
    }

    @JvmOverloads
    fun setNumber(number: Int, animation: Boolean = animationEnabled) {
        if (number == 0) {
            clear(animation)
        } else {
            setText(number.toString(), animation)
        }
    }

    @JvmOverloads
    fun clear(animation: Boolean = animationEnabled) {
        if (!isVisible) return

        if (animation) {
            binding.flContainer.startAnimation(hide)
        } else {
            isVisible = false
        }
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }

    companion object {
        private const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF
        private const val DEFAULT_TEXT_SIZE = 14
        private const val DEFAULT_ANIMATION_ENABLED = true
        private const val DEFAULT_ANIMATION_DURATION = 250
        private const val DEFAULT_MAX_TEXT_LENGTH = 2
        private const val DEFAULT_ELLIPSIZE_TEXT = "..."
    }
}