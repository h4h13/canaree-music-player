package dev.olog.msc.presentation.widget.playpause

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageButton
import dev.olog.msc.presentation.theme.AppTheme
import dev.olog.msc.presentation.utils.lazyFast
import dev.olog.msc.utils.k.extension.isPortrait
import dev.olog.shared.isDarkMode
import dev.olog.shared.textColorTertiary

@Keep
class AnimatedPlayPauseImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null

) : AppCompatImageButton(context, attrs, 0), IPlayPauseBehavior {

    private val behavior = PlayPauseBehaviorImpl(this)

    private val isDarkMode by lazyFast { context.isDarkMode() }

    init {
//        if (AppTheme.isDarkTheme()){ TODO check
//            setColorFilter(0xFF_FFFFFF.toInt())
//        }
    }

    fun setDefaultColor(){
        setColorFilter(getDefaultColor())
    }

    fun useLightImage(){
        setColorFilter(0xFF_F5F5F5.toInt())
    }

    override fun animationPlay(animate: Boolean) {
        behavior.animationPlay(animate)
    }

    override fun animationPause(animate: Boolean) {
        behavior.animationPause(animate)
    }

    private fun getDefaultColor(): Int{
        return when {
            context.isPortrait && AppTheme.isCleanTheme() && !isDarkMode -> 0xFF_8d91a6.toInt()
            AppTheme.isFullscreenTheme() || isDarkMode -> Color.WHITE
            else -> context.textColorTertiary()
        }
    }

}