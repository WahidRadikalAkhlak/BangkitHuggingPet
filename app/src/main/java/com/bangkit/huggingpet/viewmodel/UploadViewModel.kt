package com.bangkit.huggingpet.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.huggingpet.api.ApiConfig
import com.bangkit.huggingpet.dataclass.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response

class UploadViewModel : ViewModel() {
    private val _prediction = MutableLiveData<String>()
    val prediction: LiveData<String> = _prediction

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun upload(imagePart: MultipartBody.Part) {
        _isLoading.value = true
        val client = ApiConfig.getApiServiceDetect().uploadImage(imagePart)
        client.enqueue(object : retrofit2.Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                _isLoading.value = false
                val responseBody = response.body()
                if (response.isSuccessful) {
                    if (responseBody != null) {
                        _prediction.value = responseBody.prediction
                    } else {
                        _prediction.value = response.message()
                    }
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private const val TAG = "UploadViewModel"
        }
}