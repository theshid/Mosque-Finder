package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.BlogAdapter
import com.shid.mosquefinder.app.ui.main.view_models.BlogViewModel
import com.shid.mosquefinder.app.utils.enums.Status
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.android.synthetic.main.activity_beautiful_mosques.toolbar
import kotlinx.android.synthetic.main.activity_blog.*
import timber.log.Timber

class BlogActivity : BaseActivity() {
    private var previousSate = true
    private lateinit var blogAdapter: BlogAdapter
    private var mBlogList: MutableList<Article> = ArrayList()
    private val mViewModel: BlogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)
        setOnClick()
        //setNetworkMonitor()
        setObservers()
        Handler().postDelayed(kotlinx.coroutines.Runnable {
            mBlogList = mViewModel.getArticlesFromRepository()
            progressBar_blog.visibility = View.GONE
            setRecycler()
            blogAdapter.notifyDataSetChanged()
        }, 1000)
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        //mMosqueList.shuffle()
        blogAdapter = BlogAdapter()
        blog_recycler.adapter = blogAdapter
        blogAdapter.submitList(mBlogList)
    }

    private fun setObservers() {

        mViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Timber.d(it.message.toString())
                }
            }
        })
    }

   /* private fun setNetworkMonitor() {
        NetworkEvents.observe(this, androidx.lifecycle.Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            Sneaker.with(this)
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            Sneaker.with(this)
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }*/
}