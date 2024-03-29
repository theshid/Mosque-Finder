package com.shid.mosquefinder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.app.utils.helper_class.Constants.QUOTE_AUTHOR
import com.shid.mosquefinder.app.utils.helper_class.Constants.QUOTE_COLLECTION
import com.shid.mosquefinder.app.utils.helper_class.Constants.QUOTE_TEXT
import com.shid.mosquefinder.app.utils.helper_class.Constants.QUOTE_TRANSLATION
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.domain.repository.QuoteRepository
import timber.log.Timber
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(database: FirebaseFirestore) : QuoteRepository {

    private val firebaseQuoteRef: CollectionReference = database.collection(QUOTE_COLLECTION)
    private lateinit var mQuoteListEventListener: ListenerRegistration

    private var mQuoteList: MutableList<Quotes> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getQuotesFromFirebase()
    }

    override fun getQuotesFromFirebase(): MutableList<Quotes> {
        mQuoteListEventListener =
            firebaseQuoteRef.addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Timber.e("onEvent: Listen failed.", firebaseFirestoreException)
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

                        val quoteAuthor: String = doc.get(QUOTE_AUTHOR) as String
                        val quoteText: String = doc.get(QUOTE_TEXT) as String
                        val quoteFr: String = doc.get(QUOTE_TRANSLATION) as String

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
        return mQuoteList
    }
}