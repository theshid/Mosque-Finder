package com.shid.mosquefinder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.BeautifulMosques
import timber.log.Timber
import javax.inject.Inject

class BeautifulMosquesRepository @Inject constructor(val database: FirebaseFirestore) {

    private val firebaseBeautyMosqueRef: CollectionReference =
        database.collection("beautiful-mosques")
    private lateinit var mBeautyMosqueListEventListener: ListenerRegistration

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
                    Timber.e(firebaseFirestoreException, "onEvent: Listen failed.")
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

                        val mosqueName: String = doc.get("name") as String
                        val description: String = doc.get("description") as String
                        val link: String = doc.get("link") as String
                        val pic: String = doc.get("pic") as String
                        val pic2: String = doc.get("pic2") as String
                        val pic3: String = doc.get("pic3") as String
                        val descriptionFr: String = doc.get("description_fr") as String


                        val beautyElem =
                            BeautifulMosques(
                                mosqueName,
                                description,
                                link,
                                pic,
                                pic2,
                                pic3,
                                descriptionFr
                            )
                        mBeautyList.add(beautyElem)
                    }
                }
            })
        Timber.d("Mosque firebase%s", mBeautyList.isEmpty().toString())
        return mBeautyList
    }
}