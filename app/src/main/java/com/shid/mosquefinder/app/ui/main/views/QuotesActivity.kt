package com.shid.mosquefinder.app.ui.main.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.QuotesViewModelFactory
import com.shid.mosquefinder.app.ui.main.adapters.ViewPagerAdapter
import com.shid.mosquefinder.app.ui.main.view_models.QuotesViewModel
import com.shid.mosquefinder.app.utils.network.Event
import com.shid.mosquefinder.app.utils.network.NetworkEvents
import com.shid.mosquefinder.app.utils.enums.Status
import kotlinx.android.synthetic.main.activity_quotes.*
import kotlinx.android.synthetic.main.activity_quotes.toolbar

class QuotesActivity : AppCompatActivity() {

    private var mQuoteList: MutableList<Quotes> = ArrayList()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewModel: QuotesViewModel
    private lateinit var viewModelFactory: QuotesViewModelFactory
    private var previousSate = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        savedInstanceState?.let {
            previousSate = it.getBoolean("LOST_CONNECTION")
        }


        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })

        setViewModel()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            mQuoteList = viewModel.getQuotesFromRepository()
            mQuoteList.shuffle()
            progressBar.visibility = View.GONE
            setUpViewPager()
        }, 2000)
        setOnClick()
        setObservers()
        //setUpViewPager()
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setObservers() {

        viewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer{
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

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            //showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()

        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            //showSnackBar(textView, "No Network !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()

        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    private fun setUpViewPager() {
        viewPager2.setPadding(100, 0, 100, 0)
        viewPagerAdapter = ViewPagerAdapter(mQuoteList, this)
        viewPager2.adapter = viewPagerAdapter
        worm_dots_indicator.setViewPager2(viewPager2)

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