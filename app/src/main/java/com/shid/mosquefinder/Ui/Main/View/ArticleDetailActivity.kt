package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shid.mosquefinder.Data.Model.Article
import com.shid.mosquefinder.R
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout

class ArticleDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)
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