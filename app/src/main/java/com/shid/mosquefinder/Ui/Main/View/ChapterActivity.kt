package com.shid.mosquefinder.Ui.Main.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.shid.mosquefinder.R
import com.shid.mosquefinder.Ui.Main.Adapter.ChapterAdapter
import dev.kosrat.muslimdata.models.Language
import dev.kosrat.muslimdata.repository.MuslimRepository
import kotlinx.android.synthetic.main.activity_chapter.*
import kotlinx.coroutines.launch

class ChapterActivity : AppCompatActivity(),ChapterAdapter.ItemAction {
    private lateinit var adapter: ChapterAdapter
    private var categorNum: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)

        categorNum = intent.getIntExtra("category", 1)
        adapter = ChapterAdapter {  it1 -> goToItemActivity(it1) }
        setUI()
    }

    private fun setUI() {
        rv_chapter.adapter = adapter
        adapter.setItemClickAction(this)
        categorNum?.let { getChapters(it) }
    }

    private fun goToItemActivity(chapterNum: Int) {
        val intent = Intent(applicationContext, ItemActivity::class.java)
        intent.putExtra("chapter", chapterNum)
        startActivity(intent)
    }

    private fun getChapters(categoryId:Int) {
        lifecycleScope.launch {
            val repository = MuslimRepository(this@ChapterActivity)
            val azkarChapters = repository.getAzkarChapters(Language.EN, categoryId)
            if (azkarChapters != null) {
                adapter.setData(azkarChapters)
            }
            Log.i("azkarChapters", "$azkarChapters")
            Log.i("azkarChapters", "${azkarChapters?.size}")
        }
    }

    override fun clickItemAction(chapterId: Int) {
        goToItemActivity(chapterId)
    }
}