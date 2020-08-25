package com.shid.mosquefinder.Ui.Main.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Base.QuotesViewModelFactory
import com.shid.mosquefinder.Ui.Main.Adapter.ViewPagerAdapter
import com.shid.mosquefinder.Ui.Main.ViewModel.QuotesViewModel
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import kotlinx.android.synthetic.main.activity_quotes.*

class QuotesActivity : AppCompatActivity() {

    private var mQuoteList: MutableList<Quotes> = ArrayList()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewModel: QuotesViewModel
    private lateinit var viewModelFactory: QuotesViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        setTransparentStatusBar()
        setViewModel()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            mQuoteList = viewModel.getQuotesFromRepository()
            setUpViewPager()
        }, 2000)
        //setUpViewPager()
    }

    private fun setUpViewPager() {
        viewPager2.setPadding(60, 0, 60, 0)
        viewPagerAdapter = ViewPagerAdapter(mQuoteList, this)

        viewPager2.adapter = viewPagerAdapter

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    private fun setViewModel() {
        viewModelFactory = QuotesViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(QuotesViewModel::class.java)
        //mQuoteList = viewModel.getQuotesFromRepository()
    }
}