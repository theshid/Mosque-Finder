package com.shid.mosquefinder.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.app.utils.helper_class.Resource

class QuoteRepository {
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseQuoteRef: CollectionReference = database.collection("quotes")
    private lateinit var mQuoteListEventListener: ListenerRegistration

    private val TAG: String = "Quote Repository"
    private var mQuoteList: MutableList<Quotes> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getQuotesFromFirebase()
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    fun getQuotesFromFirebase(): MutableList<Quotes> {
        mQuoteListEventListener =
            firebaseQuoteRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
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

                    mQuoteList.clear()
                    for (doc in querySnapshot) {
                        val quote = doc.toObject(Quotes::class.java)

                        //quote.documentId = doc.id

                        var quoteAuthor: String = doc.get("author") as String
                        var quoteText: String = doc.get("quote") as String
                        var quoteFr:String = doc.get("quote_fr") as String
                        //var mosqueId: String = quote.documentId


                        var QuoteElem: Quotes =
                            Quotes(
                                quoteAuthor,
                                quoteText,
                                quoteFr
                            )
                        //Log.d(TAG, "the id  is" + QuoteElem.documentId)
                        mQuoteList.add(QuoteElem)
                        /* var lieu: LatLng = LatLng(locationMos.latitude,locationMos.longitude)
                         var marker : Marker = mMap.addMarker(MarkerOptions().position(lieu).title(mosqueName))*/
                        //Log.d(TAG, "mosque position" + quote.position.latitude)
                    }
                }
            })
        Log.d(TAG, "Mosque firebase" + mQuoteList.isEmpty().toString())
        return mQuoteList
    }
}