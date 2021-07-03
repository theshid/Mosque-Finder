package com.shid.mosquefinder.Ui.onboardingscreen.feature.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shid.mosquefinder.Data.Model.User
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Common
import com.shid.mosquefinder.Utils.setTransparentStatusBar

class OnBoardingActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_activity)
        setTransparentStatusBar()
        user = getUserFromIntent()
    }
    private fun getUserFromIntent(): User {
        return intent.getSerializableExtra(Common.USER) as User
    }

    companion object{
        var user: User? = null
    }

}
