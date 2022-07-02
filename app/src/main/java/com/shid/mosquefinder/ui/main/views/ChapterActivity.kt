package com.shid.mosquefinder.ui.main.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.data.repository.ChapterRepository
import com.shid.mosquefinder.data.database.QuranDatabase
import com.shid.mosquefinder.data.database.entities.Chapter
import com.shid.mosquefinder.R
import com.shid.mosquefinder.ui.Base.ChapterViewModelFactory
import com.shid.mosquefinder.ui.main.adapters.ChapterAdapter
import com.shid.mosquefinder.ui.main.view_models.ChapterViewModel
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_chapter.*
import kotlinx.android.synthetic.main.activity_chapter.toolbar
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ChapterActivity : AppCompatActivity(), ChapterAdapter.ItemAction {
    private lateinit var adapter: ChapterAdapter
    private var categorNum: Int? = null
    private var chapterList: List<Chapter>? = null
    private lateinit var viewModel: ChapterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)

        categorNum = intent.getIntExtra("category", 1)
        adapter = ChapterAdapter { it1 -> goToItemActivity(it1) }
        setViewModel()
        setUI()
        setOnClick()
    }

    private fun setViewModel() {
        val dao =
            QuranDatabase.getDatabase(application, lifecycleScope, application.resources).surahDao()
        viewModel = ViewModelProvider(
            this,
            ChapterViewModelFactory(application, ChapterRepository(dao))
        ).get(ChapterViewModel::class.java)
        categorNum?.let { viewModel.fetchChapters(it) }
    }

    private fun setUI() {
        rv_chapter.adapter = adapter
        adapter.setItemClickAction(this)
        categorNum?.let {
            if(Locale.getDefault().language.contentEquals("fr")){
              viewModel.chapters.observe(this, androidx.lifecycle.Observer {
                  Timber.d("list in Activity$it")
                  chapterList = it
                  adapter.setFrenchData(it)
              })

            }else{
                getChapters(it)
            }
             }
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun goToItemActivity(chapterNum: Int) {
        val intent = Intent(applicationContext, AzkharActivity::class.java)
        intent.putExtra("chapter", chapterNum)
        startActivity(intent)
    }

    private fun getChapters(categoryId: Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@ChapterActivity)
            val azkarChapters = repository.getAzkarChapters(Language.EN, categoryId)
            if (azkarChapters != null) {
                adapter.setData(azkarChapters)
            }
            /*Log.i("azkarChapters", "$azkarChapters")
            Log.i("azkarChapters", "${azkarChapters?.size}")*/
        }
    }

    override fun clickItemAction(chapterId: Int) {
        goToItemActivity(chapterId)
    }
}