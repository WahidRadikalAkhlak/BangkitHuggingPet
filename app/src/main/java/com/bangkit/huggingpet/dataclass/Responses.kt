package com.bangkit.huggingpet.dataclass

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.bangkit.huggingpet.database.ListPetDetail
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

data class RegisterDataAccount(
    var name: String,
    var email: String,
    var password: String
)

data class LoginDataAccount(
    var email: String,
    var password: String
)

data class ResponseDetail(
    var error: Boolean,
    var message: String
)

data class ResponseLogin(
    var error: Boolean,
    var message: String,
    var loginResult: LoginResult
)

data class LoginResult(
    var userId: String,
    var name: String,
    var token: String
)


data class UploadResponse(
    var prediction: String
)

data class Message(
    val text: String? = null,
    val name: String? = null,
    val photoUrl: String? = null,
    val timestamp: Long? = null,
)

data class ResponsePagingPet(
    @field:SerializedName("error")
    var error: String,

    @field:SerializedName("message")
    var message: String,

    @field:SerializedName("listPet")
    var listPet: List<ListPetDetail>
)
@Parcelize
data class ResponseDisease(
    @field:SerializedName("prediction")
    val prediction: String
): Parcelable