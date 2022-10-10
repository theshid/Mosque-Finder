package com.shid.mosquefinder.app.ui.main.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.SearchAdapter
import com.shid.mosquefinder.app.ui.main.view_models.SearchViewModel
import com.shid.mosquefinder.app.utils.enums.Status
import com.shid.mosquefinder.data.model.ClusterMarker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_search.*
import java.math.BigDecimal
import java.math.RoundingMode

@AndroidEntryPoint
class SearchActivity : BaseActivity(), SearchAdapter.OnClickSearch {

    companion object {
        const val SEARCH_RESULT = "search_result"
    }

    private lateinit var searchAdapter: SearchAdapter
    private val searchViewModel: SearchViewModel by viewModels()

    private var sortedMosqueList: List<ClusterMarker> = ArrayList()
    private var listFromApi = arrayListOf<ClusterMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val bundle = intent.extras
        if (bundle != null) {
            listFromApi =
                bundle.getParcelableArrayList<ClusterMarker>("test") as ArrayList<ClusterMarker>
        }
        setRecycler()
        setObservers()
        setOnClick()
        Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            progressBar.visibility = View.GONE
            sortClusterMarkerList()
            searchAdapter.list = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.mosqueList = sortedMosqueList as MutableList<ClusterMarker>
            searchAdapter.notifyDataSetChanged()
            setSearch()

        }, 1000)
    }

    private fun setSearch() {
        searchEdit.doOnTextChanged { text, _, _, _ ->
            val search = text.toString()
            searchAdapter.filter.filter(search)
        }
    }

    private fun setObservers() {

        searchViewModel.retrieveStatusMsg().observe(this, androidx.lifecycle.Observer {
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

    private fun sortClusterMarkerList() {
        sortedMosqueList = listFromApi.sortedWith(compareBy { it.distanceFromUser })
    }

    private fun calculateDistanceBetweenUserAndMosque(
        position: LatLng,
        userLocation: LatLng
    ): Double {
        val distanceInKm: Double =
            SphericalUtil.computeDistanceBetween(userLocation, position) * 0.001
        return BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }


    private fun setOnClick() {
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setRecycler() {
        searchAdapter = SearchAdapter(this, this)
        searchAdapter.setOnClickSearch {
            val returnIntent = Intent()
            returnIntent.putExtra(SEARCH_RESULT, it)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        searchRecycler.adapter = searchAdapter
    }

    override fun onClickSearch(clusterMarker: ClusterMarker) {

    }


}