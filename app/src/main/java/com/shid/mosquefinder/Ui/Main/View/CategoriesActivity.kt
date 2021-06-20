package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.activity_categories.toolbar
import kotlinx.android.synthetic.main.activity_chapter.*

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

    private fun goToChapterActivity(categoryNum:Int){
        val intent = Intent(this,ChapterActivity::class.java)
        intent.putExtra("category",categoryNum)
        startActivity(intent)
    }
}