package com.shid.mosquefinder.Ui.onboardingscreen.feature.onboarding.customView

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.omni.onboardingscreen.feature.onboarding.OnBoardingPagerAdapter
import com.omni.onboardingscreen.feature.onboarding.entity.OnBoardingPage
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.View.HomeActivity
import com.shid.mosquefinder.Ui.Main.View.MapsActivity2
import com.shid.mosquefinder.Ui.onboardingscreen.domain.OnBoardingPrefManager
import com.shid.mosquefinder.Ui.onboardingscreen.feature.onboarding.OnBoardingActivity
import com.shid.mosquefinder.Utils.Common
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.onboarding_view.view.*
import setParallaxTransformation

class OnBoardingView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val numberOfPages by lazy { OnBoardingPage.values().size }
    private val prefManager: OnBoardingPrefManager
    //private val user: User = context.

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.onboarding_view, this, true)
        setUpSlider(view)
        addingButtonsClickListeners(view.context)
        prefManager = OnBoardingPrefManager(view.context)
    }

    private fun setUpSlider(view: View) {
        with(slider) {
            adapter = OnBoardingPagerAdapter()
            setPageTransformer { page, position ->
                setParallaxTransformation(page, position)
            }

            addSlideChangeListener()

            val wormDotsIndicator = view.findViewById<WormDotsIndicator>(R.id.page_indicator)
            wormDotsIndicator.setViewPager2(this)
        }
    }


    private fun addSlideChangeListener() {

        slider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (numberOfPages > 1) {
                    val newProgress = (position + positionOffset) / (numberOfPages - 1)
                    onboardingRoot.progress = newProgress
                }
            }
        })
    }

    private fun addingButtonsClickListeners(context: Context) {
        nextBtn.setOnClickListener { navigateToNextSlide() }
        skipBtn.setOnClickListener {
            setFirstTimeLaunchToFalse()
            navigateToHomeActivity(context)
        }
        startBtn.setOnClickListener {
            setFirstTimeLaunchToFalse()
            navigateToHomeActivity(context)
        }
    }

    private fun setFirstTimeLaunchToFalse() {
        prefManager.isFirstTimeLaunch = false
    }

    private fun navigateToNextSlide() {
        val nextSlidePos: Int = slider?.currentItem?.plus(1) ?: 0
        slider?.setCurrentItem(nextSlidePos, true)
    }

    private fun navigateToHomeActivity(context: Context) {
        val intent:Intent = Intent(context,HomeActivity::class.java)
        intent.putExtra(Common.USER,OnBoardingActivity.user)
        context.startActivity(intent)

    }
}