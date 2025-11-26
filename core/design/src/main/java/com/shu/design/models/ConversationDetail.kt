package com.shu.design.models


import androidx.compose.ui.graphics.Color
import com.shu.design.ui.theme.colors

object ConversationList

data class ConversationDetail(val id: Int) {
    val color: Color
        get() = colors[id % colors.size]
}