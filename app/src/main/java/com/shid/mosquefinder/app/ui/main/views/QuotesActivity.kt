package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.ViewPagerAdapter
import com.shid.mosquefinder.app.ui.main.view_models.QuotesViewModel
import com.shid.mosquefinder.app.utils.enums.Status
import com.shid.mosquefinder.app.utils.remove
import com.shid.mosquefinder.data.model.Quotes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_quotes.*

@AndroidEntryPoint
class QuotesActivity : BaseActivity() {

    private var mQuoteList: MutableList<Quotes> = ArrayList()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val viewModel: QuotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            mQuoteList = viewModel.getQuotesFromRepository()
            mQuoteList.shuffle()
            progressBar.remove()

        }, 2000)
        setUpViewPager()
        setOnClick()
        setObservers()
        //setUpViewPager()
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setObservers() {

        viewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d("Search", it.message.toString())
                }
            }
        })


    }

    private fun setUpViewPager() {
        viewPager2.setPadding(100, 0, 100, 0)
        viewPagerAdapter = ViewPagerAdapter()
        viewPager2.adapter = viewPagerAdapter
        viewPagerAdapter.submitList(viewModel.getQuotesFromRepository().shuffled())
        worm_dots_indicator.setViewPager2(viewPager2)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

        })
    }
}