package com.shid.mosquefinder.app.ui.main.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.base.BaseActivity
import com.shid.mosquefinder.app.ui.main.adapters.ChapterAdapter
import com.shid.mosquefinder.app.ui.main.view_models.ChapterViewModel
import com.shid.mosquefinder.app.utils.extensions.startActivity
import com.shid.mosquefinder.data.local.database.entities.Chapter
import dagger.hilt.android.AndroidEntryPoint
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_chapter.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChapterActivity : BaseActivity(), ChapterAdapter.ItemAction {
    private lateinit var adapter: ChapterAdapter
    private var categorNum: Int? = null
    private var chapterList: List<Chapter>? = null
    private val viewModel: ChapterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)

        categorNum = intent.getIntExtra("category", 1)
        adapter = ChapterAdapter { it1 -> goToItemActivity(it1) }
        setData()
        setUI()
        setOnClick()
    }

    private fun setData() {
        //Handle French Translation
        categorNum?.let { viewModel.fetchChapters(it) }
    }

    private fun setUI() {
        rv_chapter.adapter = adapter
        adapter.setItemClickAction(this)
        categorNum?.let { item ->
            getChapters(item)
        }
    }

    private fun setOnClick() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun goToItemActivity(chapterNum: Int) {
        startActivity<AzkharActivity> {
            putExtra("chapter", chapterNum)
        }
    }

    private fun getChapters(categoryId: Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@ChapterActivity)
            val azkarChapters = repository.getAzkarChapters(Language.EN, categoryId)
            if (azkarChapters != null) {
                adapter.submitList(azkarChapters)
            }
            /*Log.i("azkarChapters", "$azkarChapters")
            Log.i("azkarChapters", "${azkarChapters?.size}")*/
        }
    }

    override fun clickItemAction(chapterId: Int) {
        goToItemActivity(chapterId)
    }
}