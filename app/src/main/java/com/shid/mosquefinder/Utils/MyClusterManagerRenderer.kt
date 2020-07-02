package com.shid.mosquefinder.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.widget.ImageView
import coil.ImageLoader
import coil.request.LoadRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.shid.mosquefinder.Model.ClusterMarker
import com.shid.mosquefinder.R
import java.util.concurrent.ExecutionException

class MyClusterManagerRenderer constructor(
    context: Context,
    clusterManager: ClusterManager<ClusterMarker>, map: GoogleMap?
): DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {
    private lateinit var iconGenerator: IconGenerator
    private  var imageView: ImageView? = null
    private var markerWidth: Int? = null
    private var markerHeight: Int? = null
     lateinit var mContext: Context

    init {
        mContext=context
        iconGenerator = IconGenerator(context.applicationContext)
        imageView = ImageView(context.applicationContext)
        markerWidth = (context.resources.getDimension(R.dimen.custom_marker_image)).toInt()
        markerHeight = (context.resources.getDimension(R.dimen.custom_marker_image)).toInt()
        val params:ViewGroup.LayoutParams = ViewGroup.LayoutParams(markerWidth!!, markerHeight!!)
        imageView!!.layoutParams = params
        /*imageView!!.layoutParams.width = markerWidth!!
        imageView!!.layoutParams.height = markerHeight!!*/
        val padding = context.resources.getDimension(R.dimen.custom_marker_padding).toInt()
        imageView!!.setPadding(padding,padding,padding,padding)
        iconGenerator.setContentView(imageView)
    }

    override fun onBeforeClusterItemRendered(item: ClusterMarker, markerOptions: MarkerOptions) {
        if (item.mPic == "default") {
            imageView!!.setImageResource(R.drawable.mosque)
            val icon = iconGenerator.makeIcon()
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)
        } else {
            imageView!!.setImageResource(R.drawable.user)
            val icon = iconGenerator.makeIcon()
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)


            //We create a Thread to fetch the image in the database and display it onto the map
            /*val getBitmap: com.shid.mosquefinder.Utils.MyClusterManagerRenderer.GetBitmap =
                com.shid.mosquefinder.Utils.MyClusterManagerRenderer.GetBitmap(item.mPic,mContext)
            val thread = Thread(getBitmap)
            thread.start()
            try {
                thread.join()
                val pic: Bitmap? = getBitmap.getBitmapFromThread()
                imageView!!.setImageBitmap(pic)
                val icon = iconGenerator.makeIcon()
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }*/
            //Glide.with(context).load(item.getIconPicture()).into(imageView);
        }
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarker>): Boolean {
        return false
    }

    /**
     * Update the GPS coordinate of a ClusterItem
     *
     * @param clusterMarker
     */
    fun setUpdateMarker(clusterMarker: ClusterMarker) {
        val marker = getMarker(clusterMarker)
        if (marker != null) {
            marker.position = clusterMarker.position
        }
    }

     class GetBitmap(var url: String, var context: Context) : Runnable {

         var icon: Bitmap? = null

        override fun run() {
            try {

                val loader = ImageLoader(context)
                val req = LoadRequest.Builder(context)
                    .data(url) // demo link
                    .target { result ->
                       icon = (result as BitmapDrawable).bitmap
                    }
                    .build()

                val disposable = loader.execute(req)



            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

         fun getBitmapFromThread(): Bitmap? {
             return icon
         }

    }
}