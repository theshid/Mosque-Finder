package com.shid.mosquefinder.data.repository

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.data.model.pojo.TranslationResponse
import com.shid.mosquefinder.R
import com.shid.mosquefinder.data.api.DeeplApiInterface
import com.shid.mosquefinder.data.remote.toDomain
import com.shid.mosquefinder.domain.repository.AzkharRepository
import com.shid.mosquefinder.domain.model.DeepL
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class AzkharRepositoryImpl @Inject constructor(val resources: Resources, private val deeplApiInterface: DeeplApiInterface):
    AzkharRepository {

    val service = Common.deeplApiService
    val TARGET_LG = "FR"
    var _translationResponse = MutableLiveData<TranslationResponse>()
    val translationResponse: LiveData<TranslationResponse>
        get() = _translationResponse

    override suspend fun setTranslation(input: String): Flow<DeepL> = flow {
        val response = deeplApiInterface.getTranslation(resources.getString(R.string.deep_key),input,TARGET_LG)
        emit(response.toDomain())
    }

    /*fun setTranslation(input: String){
        service.getTranslation(application.getString(R.string.deep_key), input, TARGET_LG)
            .enqueue(object : Callback<DeepLResponse> {
                override fun onResponse(call: Call<DeepLResponse>, response: Response<DeepLResponse>) {
                    Timber.d("OnResponse")
                    if (response.code() == 200) {
                        _translation.value = response.body()!!.translationResponse[0]

                        Timber.d("OnResponse OK:" + response.body()!!.translationResponse[0].textTranslation)
                    } else {
                        _translation.value =
                            Translation("EN", application.getString(R.string.translate_in_french))
                    }
                }

                override fun onFailure(call: Call<DeepLResponse>, t: Throwable) {
                    Timber.d("OnFailure")
                }

            })
    }*/


}