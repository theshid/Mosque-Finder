package com.shid.mosquefinder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.app.utils.helper_class.Resource
import timber.log.Timber
import javax.inject.Inject

class BlogRepository @Inject constructor(val database:FirebaseFirestore) {

    private val firebaseBlogRef: CollectionReference =
        database.collection("blog")
    private lateinit var mBlogListEventListener: ListenerRegistration

    private var mBlogList: MutableList<Article> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getArticlesFromFirebase()
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    fun getArticlesFromFirebase(): MutableList<Article> {
        mBlogListEventListener =
            firebaseBlogRef.addSnapshotListener(EventListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
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
                    Timber.d("querySnapshot not null")
                    mBlogList.clear()
                    for (doc in querySnapshot) {

                        val title: String = doc.get("title") as String
                        val titleFr: String = doc.get("title_fr") as String
                        val author: String = doc.get("author") as String
                        val body: String = doc.get("body") as String
                        val pic: String = doc.get("image_link") as String
                        val bodyFr: String = doc.get("body_fr") as String
                        val tag: String = doc.get("tag") as String


                        val article =
                            Article(
                                title,
                                titleFr,
                                author,
                                body,
                                pic,
                                bodyFr,
                                tag
                            )
                        mBlogList.add(article)
                        Timber.d("Is blog list empty:" + mBlogList.isEmpty())
                    }
                }
            })
        Timber.d("Is blog list empty:" + mBlogList.isEmpty())
        return mBlogList
    }
}