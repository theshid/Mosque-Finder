package com.shid.mosquefinder.Data.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.Data.Model.BeautifulMosques
import com.shid.mosquefinder.Utils.Resource

class BeautifulMosquesRepository {
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseBeautyMosqueRef: CollectionReference =
        database.collection("beautiful-mosques")
    private lateinit var mBeautyMosqueListEventListener: ListenerRegistration

    private val TAG: String = "Beauty Repository"
    private var mBeautyList: MutableList<BeautifulMosques> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getMosquesFromFirebase()
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    fun getMosquesFromFirebase(): MutableList<BeautifulMosques> {
        mBeautyMosqueListEventListener =
            firebaseBeautyMosqueRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "onEvent: Listen failed.", firebaseFirestoreException)
                    statusMsg.postValue(
                        Resource.error(
                            firebaseFirestoreException.toString(),
                            "could not load data, check internet"
                        )
                    )
                    return@EventListener
                }

                if (querySnapshot != null) {

                    mBeautyList.clear()
                    for (doc in querySnapshot) {
                        //val quote = doc.toObject(Quotes::class.java)

                        //quote.documentId = doc.id

                        val mosqueName: String = doc.get("name") as String
                        val description: String = doc.get("description") as String
                        val link: String = doc.get("link") as String
                        val pic:String = doc.get("pic") as String
                        val pic2:String = doc.get("pic2") as String
                        val pic3:String = doc.get("pic3") as String
                        val descriptionFr:String = doc.get("description_fr") as String
                        //var mosqueId: String = quote.documentId


                        var beautyElem: BeautifulMosques =
                            BeautifulMosques(
                                mosqueName,
                                description,
                                link,
                                pic,
                                pic2,
                                pic3,
                                descriptionFr
                            )
                        //Log.d(TAG, "the id  is" + QuoteElem.documentId)
                        mBeautyList.add(beautyElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        //Log.d(TAG, "mosque position" + quote.position.latitude)
                    }
                }
            })
        Log.d(TAG, "Mosque firebase" + mBeautyList.isEmpty().toString())
        return mBeautyList
    }
}