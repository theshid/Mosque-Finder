package com.shid.mosquefinder.app.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.main.views.ArticleDetailActivity
import com.shid.mosquefinder.app.utils.helper_class.Constants
import kotlinx.android.synthetic.main.item_post.view.*
import java.util.*

class BlogAdapter :
    ListAdapter<Article, BlogAdapter.ViewHolder>(DIFF_CALLBACK) {
    private var onClickedTime = System.currentTimeMillis()


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.body == newItem.body

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_post, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(article: Article) {
            itemView.apply {
                if (Locale.getDefault().language.contentEquals(Constants.FRENCH_VERSION)) {
                    post_title.text = "Titre:"+article.title_fr
                    post_author.text = "Auteur:"+article.author
                }else{
                    post_title.text = "Title:"+article.title
                    post_author.text = "Author:"+article.author
                }

                imageView.load(article.pic)

                rootView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - onClickedTime > transformationLayout_blog.duration) {
                        onClickedTime = currentTime
                        ArticleDetailActivity.startActivity(transformationLayout_blog, article)
                    }
                }

            }
        }
    }
}