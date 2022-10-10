package com.shid.mosquefinder.app.ui.main.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.MosqueViewPagerAdapter
import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder
import com.shid.mosquefinder.app.utils.network.Event
import com.shid.mosquefinder.app.utils.network.NetworkEvents
import com.shid.mosquefinder.app.utils.onTransformationEndContainerApplyParams
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_detail.*
import java.util.*

@AndroidEntryPoint
class DetailActivity : BaseActivity() {
    private var mImgLinkList: MutableList<String> = ArrayList()
    private lateinit var viewPagerAdapter: MosqueViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationEndContainerApplyParams()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setOnClick()
        val mosqueItem: BeautifulMosques = requireNotNull(intent.getParcelableExtra(EXTRA_MOSQUE))
        setUi(mosqueItem)
        setUpViewPager()
    }

    private fun setUi(beautifulMosques: BeautifulMosques) {
        name.text = beautifulMosques.name
        if (Locale.getDefault().getLanguage() == "fr") {
            description.text = beautifulMosques.description_fr
        } else {
            description.text = beautifulMosques.description
        }

        mImgLinkList.add(beautifulMosques.link)
        mImgLinkList.add(beautifulMosques.pic)
        mImgLinkList.add(beautifulMosques.pic2)
        mImgLinkList.add(beautifulMosques.pic3)

    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpViewPager() {
        viewPager2.setPadding(100, 0, 100, 0)
        viewPagerAdapter = MosqueViewPagerAdapter(mImgLinkList, this)
        viewPager2.adapter = viewPagerAdapter
        worm_dots_indicator2.setViewPager2(viewPager2)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

        })
    }


    companion object {

        private const val EXTRA_MOSQUE = "EXTRA_MOSQUE"

        fun startActivity(transformationLayout: TransformationLayout, mosque: BeautifulMosques) {
            val context = transformationLayout.context
            if (context is Activity) {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra(EXTRA_MOSQUE, mosque)
                TransformationCompat.startActivity(transformationLayout, intent)
            }
        }
    }

}