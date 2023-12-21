package com.bangkit.huggingpet.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.bangkit.huggingpet.api.ApiConfig
import com.bangkit.huggingpet.api.ApiService
import com.bangkit.huggingpet.database.ListPetDetail
import com.bangkit.huggingpet.database.PetDatabase
import com.bangkit.huggingpet.dataclass.LoginDataAccount
import com.bangkit.huggingpet.dataclass.RegisterDataAccount
import com.bangkit.huggingpet.dataclass.ResponseDetail
import com.bangkit.huggingpet.dataclass.ResponseLogin
import com.bangkit.huggingpet.espresso.wrapEspressoIdlingResource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainRepository(
    private val petDatabase: PetDatabase,
    private val apiService: ApiService
) {
    private var _pets = MutableLiveData<List<ListPetDetail>>()
    var pets: LiveData<List<ListPetDetail>> = _pets

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userLogin = MutableLiveData<ResponseLogin>()
    var userlogin: LiveData<ResponseLogin> = _userLogin

    fun getResponseLogin(loginDataAccount: LoginDataAccount) {
        wrapEspressoIdlingResource {
            _isLoading.value = true
            val api = ApiConfig.getApiService().loginUser(loginDataAccount)
            api.enqueue(object : Callback<ResponseLogin> {
                override fun onResponse(
                    call: Call<ResponseLogin>,
                    response: Response<ResponseLogin>
                ) {
                    _isLoading.value = false
                    val responseBody = response.body()

                    if (response.isSuccessful) {
                        _userLogin.value = responseBody!!
                        _message.value = "Hello ${_userLogin.value!!.loginResult.userId}!"
                    } else {
                        when (response.code()) {
                            401 -> _message.value =
                                "Email atau password yang anda masukan salah, silahkan coba lagi"
                            408 -> _message.value =
                                "Koneksi internet anda lambat, silahkan coba lagi"
                            else -> _message.value = "Pesan error: " + response.message()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                    _isLoading.value = false
                    _message.value = "Pesan error: " + t.message.toString()
                }

            })
        }
    }

    fun getResponseRegister(registDataUser: RegisterDataAccount) {
        wrapEspressoIdlingResource {
            _isLoading.value = true
            val api = ApiConfig.getApiService().registUser(registDataUser)
            api.enqueue(object : Callback<ResponseDetail> {
                override fun onResponse(
                    call: Call<ResponseDetail>,
                    response: Response<ResponseDetail>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _message.value = "Yeay akun berhasil dibuat"
                    } else {
                        when (response.code()) {
                            400 -> _message.value =
                                "Email yang anda masukan sudah terdaftar, silahkan coba lagi"
                            408 -> _message.value =
                                "Koneksi internet anda lambat, silahkan coba lagi"
                            else -> _message.value = "Pesan error: " + response.message()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseDetail>, t: Throwable) {
                    _isLoading.value = false
                    _message.value = "Pesan error: " + t.message.toString()
                }

            })
        }
    }

    fun upload(
        photo: MultipartBody.Part,
        des: RequestBody,
        token: String
    ) {
        _isLoading.value = true
        val service = ApiConfig.getApiService().addPet(
            photo, des, "Bearer $token"
        )
        service.enqueue(object : Callback<ResponseDetail> {
            override fun onResponse(
                call: Call<ResponseDetail>,
                response: Response<ResponseDetail>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _message.value = responseBody.message
                    }
                } else {
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<ResponseDetail>, t: Throwable) {
                _isLoading.value = false
                _message.value = t.message
            }
        })
    }

//    fun getStories(token: String) {
//        _isLoading.value = true
//        val api = ApiConfig.getApiService().getLocationStory(32, 1, "Bearer $token")
//        api.enqueue(object : Callback<ResponseLocationStory> {
//            override fun onResponse(
//                call: Call<ResponseLocationStory>,
//                response: Response<ResponseLocationStory>
//            ) {
//                _isLoading.value = false
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null) {
//                        _stories.value = responseBody.listStory
//                    }
//                    _message.value = responseBody?.message.toString()
//
//                } else {
//                    _message.value = response.message()
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseLocationStory>, t: Throwable) {
//                _isLoading.value = false
//                _message.value = t.message.toString()
//            }
//        })
//    }

    @ExperimentalPagingApi
    fun getPagingPets(token: String): LiveData<PagingData<ListPetDetail>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = PetRemoteMediator(petDatabase, apiService, token),
            pagingSourceFactory = {
                petDatabase.getListPetDetailDao().getAllPets()
            }
        )
        return pager.liveData
    }

}