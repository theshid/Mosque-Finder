package com.omni.onboardingscreen.feature.onboarding.entity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.shid.mosquefinder.R


enum class OnBoardingPage(
    @StringRes val titleResource: Int,
    @StringRes val subTitleResource: Int,
    @StringRes val descriptionResource: Int,
    @DrawableRes val logoResource: Int
) {

    ONE(R.string.onboarding_slide1_title, R.string.onboarding_slide1_subtitle,R.string.onboarding_slide1_desc, R.drawable.first),
    TWO(R.string.onboarding_slide2_title, R.string.onboarding_slide2_subtitle,R.string.onboarding_slide2_desc, R.drawable.second),
    THREE(R.string.onboarding_slide3_title, R.string.onboarding_slide3_subtitle,R.string.onboarding_slide3_desc, R.drawable.third),
    FOUR(R.string.onboarding_slide4_title, R.string.onboarding_slide4_subtitle,R.string.onboarding_slide4_desc, R.drawable.fourth),
    FIVE(R.string.onboarding_slide5_title, R.string.onboarding_slide5_subtitle,R.string.onboarding_slide5_desc, R.drawable.fifth),
    SIX(R.string.onboarding_slide6_title, R.string.onboarding_slide6_subtitle,R.string.onboarding_slide6_desc, R.drawable.sith)
}