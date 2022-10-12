package com.shid.mosquefinder.app.ui.main.views


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.MosqueAdapter
import com.shid.mosquefinder.app.ui.main.view_models.BeautifulMosquesViewModel
import com.shid.mosquefinder.app.utils.enums.Status
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.skydoves.transformationlayout.onTransformationStartContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_beautiful_mosques.*

@AndroidEntryPoint
class BeautifulMosquesActivity : BaseActivity() {
    private lateinit var mosqueAdapter: MosqueAdapter
    private val mosqueViewModel: BeautifulMosquesViewModel by viewModels()
    private lateinit var layoutManager: GridLayoutManager
    private var mMosqueList: MutableList<BeautifulMosques> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beautiful_mosques)

        setOnClick()
        setObservers()
        Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            //anything you want to start after 3s
            mMosqueList = mosqueViewModel.getMosquesFromRepository()
            progressBar2.visibility = View.GONE
            setRecycler()
            mosqueAdapter.notifyDataSetChanged()
            // addUserMarker()

        }, 2000)
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        layoutManager = GridLayoutManager(this, 2)
        mosque_recycler.layoutManager = layoutManager
        mMosqueList.shuffle()
        mosqueAdapter = MosqueAdapter(mMosqueList, this)
        mosque_recycler.adapter = mosqueAdapter
    }

    private fun setObservers() {

        mosqueViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    //Handle Error

                    Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                    Log.d("Search", it.message.toString())
                }
            }
        })


    }

}