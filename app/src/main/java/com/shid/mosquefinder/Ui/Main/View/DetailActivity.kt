package com.shid.mosquefinder.Ui.Main.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.api.load
import com.irozon.sneaker.Sneaker
import com.shid.mosquefinder.ConnectivityStateHolder
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Utils.Network.Event
import com.shid.mosquefinder.Utils.Network.NetworkEvents
import com.shid.mosquefinder.Utils.onTransformationEndContainerApplyParams
import com.shid.mosquefinder.Utils.setTransparentStatusBar
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.toolbar

class DetailActivity : AppCompatActivity() {
    private var previousSate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationEndContainerApplyParams()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setTransparentStatusBar()
        setNetworkMonitor()
        setOnClick()
        val mosqueItem: BeautifulMosques = requireNotNull(intent.getParcelableExtra(EXTRA_MOSQUE))
        setUi(mosqueItem)
    }

    private fun setUi(beautifulMosques: BeautifulMosques) {
        name.text = beautifulMosques.name
        image.load(beautifulMosques.link)
        description.text = beautifulMosques.description
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
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