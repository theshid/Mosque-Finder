package com.shid.mosquefinder.app.ui.main.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import coil.load
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.utils.extensions.parcelable
import com.shid.mosquefinder.app.utils.helper_class.Constants.FRENCH_VERSION
import com.shid.mosquefinder.app.utils.onTransformationEndContainerApplyParams
import com.shid.mosquefinder.data.model.Article
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_article_detail.*
import kotlinx.android.synthetic.main.activity_detail.toolbar
import kotlinx.android.synthetic.main.content_post_details.*
import java.util.*

@AndroidEntryPoint
class ArticleDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationEndContainerApplyParams()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
        val articleItem: Article = requireNotNull(intent.parcelable(EXTRA_ARTICLE))
        setUi(articleItem)
    }

    private fun setUi(article: Article) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val formatText = article.body.replace("\\n", "\n")
        post_author.text = article.author
        imageView.load(article.pic)
        if (Locale.getDefault().language == FRENCH_VERSION) {
            post_body.text = article.body_fr.replace("\\n", "\n")
            post_title.text = article.title_fr
        } else {
            post_body.text = formatText
            post_title.text = article.title
        }

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