package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shid.mosquefinder.Data.Model.Article
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.View.ArticleDetailActivity
import kotlinx.android.synthetic.main.item_mosque.view.*
import kotlinx.android.synthetic.main.item_post.view.*

class BlogAdapter(val list: MutableList<BeautifulMosques>, val context: Context) :
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
        val layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_post, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(article: Article) {
            itemView.apply {
                post_title.text = article.title
                post_author.text = article.author
                imageView.load(article.pic)

                rootView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - onClickedTime > transformationLayout.duration) {
                        onClickedTime = currentTime
                        ArticleDetailActivity.startActivity(transformationLayout, article)
                    }
                }

            }
        }
    }
}