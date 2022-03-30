package com.work.mise.data.model.airquality

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName
import com.work.mise.R

enum class Grade(
    val label: String,
    @DrawableRes val emoji: Int,
    @ColorRes val colorResId: Int
) {
    @SerializedName("1")
    GOOD("좋음", R.drawable.mise_1, R.color.white),

    @SerializedName("2")
    NORMAL("보통",R.drawable.mise_2,R.color.white),

    @SerializedName("3")
    BAD("나쁨",R.drawable.mise_3,R.color.white),

    @SerializedName("4")
    AWFUL("매우 나쁨",R.drawable.mise_4,R.color.white),

    UNKNOWN("미측정",R.drawable.mise_4,R.color.white);

    override fun toString(): String {
        return "$emoji"
    }
}
