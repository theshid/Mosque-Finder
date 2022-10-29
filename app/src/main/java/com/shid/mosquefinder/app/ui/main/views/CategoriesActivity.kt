package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants.EXTRA_CATEGORY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_categories.*

@AndroidEntryPoint
class CategoriesActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        setClickListeners()
    }

    private fun setClickListeners() {
        cardMorning.setOnClickListener {
            goToChapterActivity(1)
        }
        cardHome.setOnClickListener {
            goToChapterActivity(2)
        }
        cardFood.setOnClickListener {
            goToChapterActivity(3)
        }
        cardJoy.setOnClickListener {
            goToChapterActivity(4)
        }
        cardTravel.setOnClickListener {
            goToChapterActivity(5)
        }
        cardPrayer.setOnClickListener {
            goToChapterActivity(6)
        }
        cardPraising.setOnClickListener {
            goToChapterActivity(7)
        }
        cardHajj.setOnClickListener {
            goToChapterActivity(8)
        }
        cardEtiquette.setOnClickListener {
            goToChapterActivity(9)
        }
        cardNature.setOnClickListener {
            goToChapterActivity(10)
        }
        cardSickness.setOnClickListener {
            goToChapterActivity(11)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun goToChapterActivity(categoryNum: Int) {
        startActivity<ChapterActivity> {
            putExtra(EXTRA_CATEGORY, categoryNum)
        }
    }
}