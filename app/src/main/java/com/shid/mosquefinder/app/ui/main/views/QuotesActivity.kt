package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.ViewPagerAdapter
import com.shid.mosquefinder.app.ui.main.states.QuoteViewState
import com.shid.mosquefinder.app.ui.main.view_models.QuotesViewModel
import com.shid.mosquefinder.app.utils.hide
import com.shid.mosquefinder.app.utils.remove
import com.shid.mosquefinder.app.utils.show
import com.shid.mosquefinder.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_blog.*
import kotlinx.android.synthetic.main.activity_quotes.*
import kotlinx.android.synthetic.main.activity_quotes.toolbar

@AndroidEntryPoint
class QuotesActivity : BaseActivity() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val viewModel: QuotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)
        setUpViewPager()
        setOnClick()
        Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            viewModel.getQuotes()
            progressBar.remove()

        }, 2000)
        setObservers()
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setObservers() {
        viewModel.quoteViewState.observe(this) { state ->
            handleQuotesLoading(state)
            state.quotes?.let { list ->
                if (list.isNotEmpty()) {
                    viewPagerAdapter.submitList(list.shuffled())
                    worm_dots_indicator.setViewPager2(viewPager2)
                }
                handleQuotesError(state)
            }
        }
    }

    private fun handleQuotesError(state: QuoteViewState) {
        state.error?.run {
            showSnackbar(blog_recycler, getString(this.message), isError = true)
        }
    }

    private fun handleQuotesLoading(blogViewState: QuoteViewState) {
        if (blogViewState.isLoading) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun setUpViewPager() {
        viewPager2.setPadding(100, 0, 100, 0)
        viewPagerAdapter = ViewPagerAdapter()
        viewPager2.adapter = viewPagerAdapter
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

        })
    }
}