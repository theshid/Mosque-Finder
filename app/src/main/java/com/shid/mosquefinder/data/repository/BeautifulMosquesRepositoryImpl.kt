package com.shid.mosquefinder.data.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_COLLECTION
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_DESCRIPTION
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_DESCRIPTION_FR
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_LINK
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_NAME
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_PIC
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_PIC2
import com.shid.mosquefinder.app.utils.helper_class.Constants.B_MOSQUE_PIC3
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.domain.repository.BeautifulMosquesRepository
import timber.log.Timber
import javax.inject.Inject

class BeautifulMosquesRepositoryImpl @Inject constructor(val database: FirebaseFirestore) :
    BeautifulMosquesRepository {

    private val firebaseBeautyMosqueRef: CollectionReference =
        database.collection(B_MOSQUE_COLLECTION)
    private lateinit var mBeautyMosqueListEventListener: ListenerRegistration

    private var mBeautyList: MutableList<BeautifulMosques> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getMosquesFromFirebase()
    }

    override fun getMosquesFromFirebase(): MutableList<BeautifulMosques> {
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

                        val mosqueName: String = doc.get(B_MOSQUE_NAME) as String
                        val description: String = doc.get(B_MOSQUE_DESCRIPTION) as String
                        val link: String = doc.get(B_MOSQUE_LINK) as String
                        val pic: String = doc.get(B_MOSQUE_PIC) as String
                        val pic2: String = doc.get(B_MOSQUE_PIC2) as String
                        val pic3: String = doc.get(B_MOSQUE_PIC3) as String
                        val descriptionFr: String = doc.get(B_MOSQUE_DESCRIPTION_FR) as String


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