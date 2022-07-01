package com.shid.mosquefinder.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.data.model.Pojo.DeepL
import com.shid.mosquefinder.data.model.Pojo.Translation
import com.shid.mosquefinder.R
import com.shid.mosquefinder.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AzkharRepository(val application: Application) {

    val service = Common.deeplApiService
    val TARGET_LG = "FR"
    var _translation = MutableLiveData<Translation>()
    val translation: LiveData<Translation>
        get() = _translation

    fun setTranslation(input: String){
        service.getTranslation(application.getString(R.string.deep_key), input, TARGET_LG)
            .enqueue(object : Callback<DeepL> {
                override fun onResponse(call: Call<DeepL>, response: Response<DeepL>) {
                    Log.d("Azkhar","OnResponse")
                    if (response.code() == 200) {
                        _translation.value = response.body()!!.translationResponse[0]

                        Log.d("Azkhar","OnResponse OK:"+response.body()!!.translationResponse[0].textTranslation)
                    } else{
                        _translation.value = Translation("EN",application.getString(R.string.translate_in_french))
                    }
                }

                override fun onFailure(call: Call<DeepL>, t: Throwable) {
                    Log.d("Azkhar","OnFailure")
                }

            })
    }


}