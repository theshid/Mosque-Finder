package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.BlogAdapter
import com.shid.mosquefinder.app.ui.main.states.BlogViewState
import com.shid.mosquefinder.app.ui.main.view_models.BlogViewModel
import com.shid.mosquefinder.app.utils.hide
import com.shid.mosquefinder.app.utils.show
import com.shid.mosquefinder.app.utils.showSnackbar
import com.shid.mosquefinder.data.model.Article
import com.skydoves.transformationlayout.onTransformationStartContainer
import kotlinx.android.synthetic.main.activity_beautiful_mosques.toolbar
import kotlinx.android.synthetic.main.activity_blog.*

class BlogActivity : BaseActivity() {

    private lateinit var blogAdapter: BlogAdapter
    private var mBlogList: List<Article> = ArrayList()
    private val mViewModel: BlogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)
        setOnClick()
        //setNetworkMonitor()

        Handler().postDelayed(kotlinx.coroutines.Runnable {
            //mBlogList = mViewModel.getArticlesFromRepository()
            progressBar_blog.visibility = View.GONE
            setRecycler()
            setObservers()
            //blogAdapter.notifyDataSetChanged()
        }, 1000)
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        blogAdapter = BlogAdapter()
        blog_recycler.adapter = blogAdapter
        //blogAdapter.submitList(mBlogList)
    }

    private fun setObservers() {
        mViewModel.blogViewState.observe(this) { state ->
            handleArticleLoading(state)
            state.articles?.let { list ->
                if (list.isNotEmpty()) {
                    blogAdapter.submitList(list)
                }
                handleArticleError(state)
            }
        }
    }

    private fun handleArticleError(state: BlogViewState) {
        state.error?.run {
            showSnackbar(blog_recycler, getString(this.message), isError = true)
        }
    }

    private fun handleArticleLoading(blogViewState: BlogViewState) {
        if (blogViewState.isLoading) {
            progressBar_blog.show()
        } else {
            progressBar_blog.hide()
        }
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