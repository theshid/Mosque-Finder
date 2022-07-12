package com.shid.mosquefinder.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.domain.repository.QuoteRepository
import timber.log.Timber
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(database: FirebaseFirestore) :QuoteRepository{

    private val firebaseQuoteRef: CollectionReference = database.collection("quotes")
    private lateinit var mQuoteListEventListener: ListenerRegistration

    private var mQuoteList: MutableList<Quotes> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getQuotesFromFirebase()
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    override fun getQuotesFromFirebase(): MutableList<Quotes> {
        mQuoteListEventListener =
            firebaseQuoteRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Timber.e( "onEvent: Listen failed.", firebaseFirestoreException)
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
                        //val quote = doc.toObject(Quotes::class.java)

                        //quote.documentId = doc.id

                        val quoteAuthor: String = doc.get("author") as String
                        val quoteText: String = doc.get("quote") as String
                        val quoteFr:String = doc.get("quote_fr") as String
                        //var mosqueId: String = quote.documentId


                        val quoteElem =
                            Quotes(
                                quoteAuthor,
                                quoteText,
                                quoteFr
                            )
                        mQuoteList.add(quoteElem)
                    }
                }
            })
        Timber.d( "Mosque firebase" + mQuoteList.isEmpty().toString())
        return mQuoteList
    }
}