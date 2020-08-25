package com.shid.mosquefinder.Ui.Main.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.shid.mosquefinder.Data.Model.Quotes
import com.shid.mosquefinder.R
import kotlinx.android.synthetic.main.item_pager.view.*

class ViewPagerAdapter(var list: List<Quotes>, var context: Context): PagerAdapter() {

private lateinit var layoutInflater:LayoutInflater
    override fun getCount(): Int = list.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_pager, container, false)

        view.author.text = list[position].author
        view.quote_text.text = list[position].quote


        view.setOnClickListener {
            /*val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("param", models.get(position).getTitle())
            context.startActivity(intent)*/
            // finish();
        }

        container.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object`as View)
    }
}