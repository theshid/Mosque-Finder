package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import coil.api.load
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.Adapter.MosqueViewPagerAdapter
import com.shid.mosquefinder.Ui.Main.Adapter.ViewPagerAdapter
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.onTransformationEndContainerApplyParams
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.toolbar
import kotlinx.android.synthetic.main.activity_detail.viewPager2
import kotlinx.android.synthetic.main.activity_quotes.*
import java.util.*
import kotlin.collections.ArrayList

class DetailActivity : AppCompatActivity() {
    private var previousSate = true
    private var mImgLinkList: MutableList<String> = ArrayList()
    private lateinit var viewPagerAdapter: MosqueViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationEndContainerApplyParams()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setNetworkMonitor()
        setOnClick()
        val mosqueItem: BeautifulMosques = requireNotNull(intent.getParcelableExtra(EXTRA_MOSQUE))
        setUi(mosqueItem)
        setUpViewPager()
    }

    private fun setUi(beautifulMosques: BeautifulMosques) {
        name.text = beautifulMosques.name
        if (Locale.getDefault().getLanguage() == "fr"){
            description.text = beautifulMosques.description_fr
        }else{
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


    private fun setNetworkMonitor() {
        NetworkEvents.observe(this, androidx.lifecycle.Observer {

            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()


        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected && !previousSate) {
            // showSnackBar(textView, "The network is back !")
            Sneaker.with(this) // Activity, Fragment or ViewGroup
                .setTitle(getString(R.string.sneaker_connected))
                .setMessage(getString(R.string.sneaker_msg_network))
                .sneakSuccess()
        }

        if (!ConnectivityStateHolder.isConnected && previousSate) {
            // showSnackBar(textView, "No Network !")
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