package com.shid.mosquefinder.data.repository


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.data.model.Api.GoogleApiInterface
import com.shid.mosquefinder.data.model.Mosque
import com.shid.mosquefinder.data.model.User
import com.shid.mosquefinder.data.model.pojo.GoogleMosque
import com.shid.mosquefinder.data.model.pojo.Place
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class MapRepository @Inject constructor(
    private val mService: GoogleApiInterface,
    @ApplicationContext val application: Context
) {
    @Inject
    lateinit var database: FirebaseFirestore
    private val firebaseMosqueRef: CollectionReference = database.collection("mosques")
    private lateinit var mMosqueListEventListener: ListenerRegistration
    private val realDatabase: DatabaseReference = Firebase.database.reference

    private val firebaseGoogleMosqueRef: CollectionReference = database.collection("test")
    private val firebaseNigerGoogleMosqueRef: CollectionReference = database.collection("niamey")
    private lateinit var mGoogleMosqueListEventListener: ListenerRegistration
    var mGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()
    var mNigerGoogleMosqueList: MutableList<GoogleMosque> = ArrayList()

    private val TAG: String = "Map Repository"
    private val mApp: Context = application
    val crashlytics = FirebaseCrashlytics.getInstance()

    var mMosqueList: MutableList<Mosque> = ArrayList()
    val placeData: MutableLiveData<Place> =
        MutableLiveData<Place>()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getGoogleMosqueFromFirebase()
        getTotalMosquesFromFirebase()
        getNigerGoogleMosqueFromFirebase()
    }


    fun getNigerGoogleMosqueFromFirebase(): MutableList<GoogleMosque> {
        mGoogleMosqueListEventListener =
            firebaseNigerGoogleMosqueRef.addSnapshotListener(EventListener<QuerySnapshot>
            { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
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

                    mNigerGoogleMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(GoogleMosque::class.java)

                        //mosque.documentId = doc.id

                        val mosqueLat: Double = doc.get("Latitude") as Double
                        val mosqueLg = doc.get("Longitude") as Double
                        val mosqueId: String = doc.get("Place ID") as String
                        val mosqueName: String = doc.get("Place Name") as String


                        val mosqueElem: GoogleMosque =
                            GoogleMosque(
                                mosqueLat,
                                mosqueLg,
                                mosqueId,
                                mosqueName
                            )
                        //Log.d(TAG, "the id  is" + mosqueElem.documentId)
                        mNigerGoogleMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        //Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })

        Log.d(TAG, "Mosque firebase" + mMosqueList.isEmpty().toString())
        return mNigerGoogleMosqueList
    }

    fun getGoogleMosqueFromFirebase(): MutableList<GoogleMosque> {
        mGoogleMosqueListEventListener =
            firebaseGoogleMosqueRef.addSnapshotListener(EventListener<QuerySnapshot>
            { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
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

                    mGoogleMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(GoogleMosque::class.java)

                        //mosque.documentId = doc.id

                        val mosqueLat: Double = doc.get("Latitude") as Double
                        val mosqueLg = doc.get("Longitude") as Double
                        val mosqueId: String = doc.get("Place ID") as String
                        val mosqueName: String = doc.get("Place Name") as String


                        val mosqueElem: GoogleMosque =
                            GoogleMosque(
                                mosqueLat,
                                mosqueLg,
                                mosqueId,
                                mosqueName
                            )
                        //Log.d(TAG, "the id  is" + mosqueElem.documentId)
                        mGoogleMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        //Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })
        Log.d(TAG, "Mosque firebase" + mMosqueList.isEmpty().toString())
        return mGoogleMosqueList
    }

    fun getTotalMosquesFromFirebase(): MutableList<Mosque> {
        mMosqueListEventListener =
            firebaseMosqueRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
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

                    mMosqueList.clear()
                    for (doc in querySnapshot) {
                        val mosque = doc.toObject(Mosque::class.java)

                        mosque.documentId = doc.id

                        val mosqueName: String = doc.get("name") as String
                        val locationMos: GeoPoint = doc.get("position") as GeoPoint
                        val mosqueId: String = mosque.documentId
                        val reportIndex: Long = doc.get("report") as Long

                        val mosqueElem: Mosque =
                            Mosque(
                                mosqueName,
                                locationMos,
                                mosqueId,
                                reportIndex
                            )
                        Log.d(TAG, "the id  is" + mosqueElem.documentId)
                        mMosqueList.add(mosqueElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        Log.d(TAG, "mosque position" + mosque.position.latitude)
                    }
                }
            })
        Log.d(TAG, "Mosque firebase" + mMosqueList.isEmpty().toString())
        return mMosqueList
    }


    private fun getRequestUrl(
        latitude: Double,
        longitude: Double,
        place: String,
        token: String = ""
    ): String {
        val googlePlaceUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=3000")
        googlePlaceUrl.append("&type=$place")
        googlePlaceUrl.append("&key=" + mApp.getString(R.string.browser_key))
        googlePlaceUrl.append("&pagetoken=$token")
        Log.d("Url_debug", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    fun googlePlaceNearbyMosques(
        keyword: String,
        userPosition: LatLng,
        nextToken: String = ""
    )
            : MutableLiveData<Place> {

        //mMap.clear()

        //Build url request base on location
        val requestUrl =
            getRequestUrl(userPosition.latitude, userPosition.longitude, keyword, nextToken)

        mService.getNearbyPlaces(requestUrl)
            .enqueue(object : Callback<Place> {
                override fun onFailure(call: Call<Place>, t: Throwable) {
                    /*Toast.makeText(mApp.applicationContext, "" + t.message, Toast.LENGTH_LONG)
                        .show()*/
                    Timber.d("failed")
                    placeData.value = null
                }

                override fun onResponse(call: Call<Place>, response: Response<Place>) {
                    Timber.d("Api Mosque" + response.isSuccessful)

                    if (response.isSuccessful) { //for(i in 0 until response.body()!!.results!!.size)
                        placeData.value = response.body()
                        placeData.postValue(response.body())


                    }

                }

            })

        return placeData
    }

    fun inputMosqueInDatabase(userInput: HashMap<String, Comparable<*>>) {
        database.collection("mosques").document()
            .set(userInput)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                statusMsg.postValue(Resource.success(mApp.getString(R.string.mosque_added)))
                //addMapMarkers()

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                crashlytics.recordException(e)
                statusMsg.postValue(
                    Resource.error(
                        e.localizedMessage,
                        mApp.getString(R.string.mosque_not_added)
                    )
                )

            }
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    fun confirmMosqueLocation(marker: Marker, user: User) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {
                val reportIndex = mosque.report + 1


                realDatabase.child("votes").child(mosque.documentId)
                    .child(user.uid).setValue(1L)
                    .addOnSuccessListener {
                        statusMsg.postValue(Resource.success(mApp.getString(R.string.location_after_confirm)))
                        database.collection("mosques").document(mosque.documentId)
                            .update("report", FieldValue.increment(1))
                            .addOnSuccessListener {
                                //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                crashlytics.recordException(it)
                            }


                    }
                    .addOnFailureListener {
                        statusMsg.postValue(
                            Resource.error(
                                it.localizedMessage,
                                mApp.getString(R.string.location_already_confirm)
                            )
                        )
                        crashlytics.recordException(it)
                    }


            }

        }
    }


    fun reportFalseLocation(marker: Marker, user: User) {
        for (mosque in mMosqueList) {
            if (marker.title == mosque.name) {

                realDatabase.child("votes").child(mosque.documentId)
                    .child(user.uid).setValue(-1L)
                    .addOnSuccessListener {
                        statusMsg.postValue(Resource.success(mApp.getString(R.string.location_after_report)))
                        database.collection("mosques").document(mosque.documentId)
                            .update("report", FieldValue.increment(-1))
                            .addOnSuccessListener {
                                //Toast.makeText(this@MapsActivity, "Thanks", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {

                                crashlytics.recordException(it)
                                Log.d(
                                    "Error",
                                    it.message.toString() + it.localizedMessage.toString()
                                )
                            }
                    }
                    .addOnFailureListener {
                        statusMsg.postValue(
                            Resource.error(
                                it.localizedMessage,
                                mApp.getString(R.string.location_already_report)
                            )
                        )
                        crashlytics.recordException(it)
                    }


            }
        }
    }
}