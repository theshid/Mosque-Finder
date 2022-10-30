package com.shid.mosquefinder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.shid.mosquefinder.app.utils.helper_class.Constants
import com.shid.mosquefinder.app.utils.helper_class.Resource
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.domain.repository.BlogRepository
import timber.log.Timber
import javax.inject.Inject

class BlogRepositoryImpl @Inject constructor(val database: FirebaseFirestore) : BlogRepository {

    private val firebaseBlogRef: CollectionReference =
        database.collection(Constants.BLOG_COLLECTION_PATH)
    private lateinit var mBlogListEventListener: ListenerRegistration

    private var mBlogList: MutableList<Article> = ArrayList()

    private val statusMsg: MutableLiveData<Resource<String>> = MutableLiveData()

    init {
        getArticlesFromFirebase()
    }

    fun returnStatusMsg(): LiveData<Resource<String>> {
        return statusMsg
    }

    override fun getArticlesFromFirebase(): MutableList<Article> {
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

                        val title: String = doc.get(Constants.BLOG_FIELD_TITLE) as String
                        val titleFr: String = doc.get(Constants.BLOG_FIELD_TITLE_FR) as String
                        val author: String = doc.get(Constants.BLOG_FIELD_AUTHOR) as String
                        val body: String = doc.get(Constants.BLOG_FIELD_BODY) as String
                        val pic: String = doc.get(Constants.BLOG_FIELD_IMAGE) as String
                        val bodyFr: String = doc.get(Constants.BLOG_FIELD_BODY_FR) as String
                        val tag: String = doc.get(Constants.BLOG_FIELD_TAG) as String

                        Timber.d("title:$title")
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

                    }

                }

            })

        Timber.d("Is blog list empty:" + mBlogList)
        return mBlogList
    }
}