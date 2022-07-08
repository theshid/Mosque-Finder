package com.shid.mosquefinder.app.ui.main.views

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.network.Event
import com.shid.mosquefinder.app.utils.network.NetworkEvents
import com.shid.mosquefinder.app.utils.onTransformationEndContainerApplyParams
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.activity_detail.toolbar
import kotlinx.android.synthetic.main.content_post_details.*
import java.util.*

class ArticleDetailActivity : AppCompatActivity() {
    private var previousSate = true
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationEndContainerApplyParams()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        setNetworkMonitor()
        val articleItem: Article = requireNotNull(intent.getParcelableExtra(EXTRA_ARTICLE))
        setUi(articleItem)
    }

    private fun setUi(article: Article) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val formatText = article.body.replace("\\n","\n")
        post_author.text = article.author
        imageView.load(article.pic)
        if (Locale.getDefault().language == "fr"){
            post_body.text = article.body_fr.replace("\\n","\n")
            post_title.text = article.title_fr
        }else{
            post_body.text = formatText
            post_title.text = article.title
        }

    }

    private fun setNetworkMonitor() {
        NetworkEvents.observe(this, androidx.lifecycle.Observer {

            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_disconnected))
                .setMessage(getString(R.string.sneaker_msg_network_lost))
                .sneakError()
        }

        previousSate = ConnectivityStateHolder.isConnected
    }

    override fun onResume() {
        super.onResume()
        handleConnectivityChange()
    }

    companion object {
        private const val EXTRA_ARTICLE = "EXTRA_ARTICLE"

        fun startActivity(transformationLayout: TransformationLayout, article: Article) {
            val context = transformationLayout.context
            if (context is Activity) {
                val intent = Intent(context, ArticleDetailActivity::class.java)
                intent.putExtra(EXTRA_ARTICLE, article)
                TransformationCompat.startActivity(transformationLayout, intent)
            }
        }
    }
}