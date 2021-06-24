package com.shid.mosquefinder.Data.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.Data.Model.Pojo.Root
import com.shid.mosquefinder.Data.Model.Pojo.Translation
import com.shid.mosquefinder.Data.Model.Pojo.Verse
import com.shid.mosquefinder.Data.Model.Pojo.Verset
import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.QuranDatabase
import com.shid.mosquefinder.Data.database.entities.Ayah
import com.shid.mosquefinder.Utils.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AyahRepository(private val quranDao: QuranDao) {

   /* val service = Common.frenchQuranApiService
    var _translation = MutableLiveData<List<Verse>>()
    val translation: LiveData<List<Verse>>
        get() = _translation*/

    fun getAyah(surahNumber: Int): List<Ayah> {
        return quranDao.getAyah(surahNumber)
    }

    fun updateAyah(text:String,ayahId:Long){
        quranDao.updateAyah(text,ayahId)
    }

   /* fun getFrenchSurah(ayahId: Int) {
        service.getFrenchSurah(ayahId).enqueue(object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.code() == 200) {
                    _translation.value = response.body()!!.data.verse
                   *//* GlobalScope.launch(Dispatchers.IO){
                        quranDao.updateAyah(response.body()!!.data.verse,ayahId)
                    }*//*

                    Log.d("Ayah", "OnResponse OK : " + response.body()!!.data.verse + " "+ayahId)
                } else {

                    Log.d("Ayah", "OnResponse Fail : ")
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {

                Log.d("Ayah", "OnResponse Fail : ")
            }

        })
    }*/
}