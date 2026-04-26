package com.javierorraca.laplog.ui

import androidx.compose.ui.graphics.Color
import com.javierorraca.laplog.data.AppTheme

data class Palette(
    val theme: AppTheme,
    val dark: Boolean,
    val bg: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    val sep: Color,
    val chipBg: Color,
)

fun paletteFor(theme: AppTheme): Palette =
    when (theme) {
        AppTheme.WarmLight -> Palette(
            theme = theme,
            dark = false,
            bg = Color(0xfff5f2ec),
            card = Color.White,
            text = Color(0xff1b1b1a),
            muted = Color(0x8c1e1c18),
            faint = Color(0x4d1e1c18),
            sep = Color(0x141e1c18),
            chipBg = Color(0x0d1e1c18),
        )
        AppTheme.Paper -> Palette(
            theme = theme,
            dark = false,
            bg = Color(0xffece7d8),
            card = Color(0xfff7f3e6),
            text = Color(0xff2a2417),
            muted = Color(0x8c2a2417),
            faint = Color(0x4d2a2417),
            sep = Color(0x1a2a2417),
            chipBg = Color(0x0d2a2417),
        )
        AppTheme.Dark -> Palette(
            theme = theme,
            dark = true,
            bg = Color(0xff17130a),
            card = Color(0xff1f1a10),
            text = Color(0xffece7d8),
            muted = Color(0x8cece7d8),
            faint = Color(0x4dece7d8),
            sep = Color(0x1aece7d8),
            chipBg = Color(0x0fece7d8),
        )
    }

fun colorFromHex(hex: Long): Color = Color((0xff000000 or hex).toInt())

fun isLightHex(hex: Long): Boolean {
    val r = (hex shr 16) and 0xff
    val g = (hex shr 8) and 0xff
    val b = hex and 0xff
    val l = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    return l > 0.7
}
