package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.extensions.startActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_categories.*

@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        setClickListeners()
    }

    private fun setClickListeners() {
        cardMorning.setOnClickListener(View.OnClickListener {
            goToChapterActivity(1)
        })
        cardHome.setOnClickListener(View.OnClickListener {
            goToChapterActivity(2)
        })
        cardFood.setOnClickListener(View.OnClickListener {
            goToChapterActivity(3)
        })
        cardJoy.setOnClickListener(View.OnClickListener {
            goToChapterActivity(4)
        })
        cardTravel.setOnClickListener(View.OnClickListener {
            goToChapterActivity(5)
        })
        cardPrayer.setOnClickListener(View.OnClickListener {
            goToChapterActivity(6)
        })
        cardPraising.setOnClickListener(View.OnClickListener {
            goToChapterActivity(7)
        })
        cardHajj.setOnClickListener(View.OnClickListener {
            goToChapterActivity(8)
        })
        cardEtiquette.setOnClickListener(View.OnClickListener {
            goToChapterActivity(9)
        })
        cardNature.setOnClickListener(View.OnClickListener {
            goToChapterActivity(10)
        })
        cardSickness.setOnClickListener(View.OnClickListener {
            goToChapterActivity(11)
        })

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun goToChapterActivity(categoryNum: Int) {
        startActivity<ChapterActivity> {
            putExtra("category", categoryNum)
        }
    }
}