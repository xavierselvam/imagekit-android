package com.imagekit.android.retrofit

import android.content.Context
import com.imagekit.android.entity.SignatureResponse
import com.imagekit.android.util.SharedPrefUtil
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadApi @Inject constructor(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil
) {
    fun getFileUploadCall(
        signatureResponse: SignatureResponse,
        file: Any,
        fileName: String,
        useUniqueFilename: Boolean,
        tags: Array<String>?,
        folder: String?,
        isPrivateFile: Boolean?,
        customCoordinates: String?,
        responseFields: String?,
        expire: String
    ): Observable<ResponseBody> {

        val commaSeparatedTags = getCommaSeparatedTagsFromTags(tags)

        val profileImagePart: MultipartBody.Part

        profileImagePart = if (file is File) {
            val fileBody = RequestBody.create(MediaType.parse("image/png"), file.readBytes())
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part.createFormData(
                "file",
                fileName,
                fileBody
            )
        } else {
            MultipartBody.Part.createFormData("file", file as String)
        }

        return NetworkManager
            .getApiInterface()
            .uploadImage(
                profileImagePart,
                MultipartBody.Part.createFormData(
                    "publicKey",
                    sharedPrefUtil.getClientPublicKey()
                ),
                MultipartBody.Part.createFormData("signature", signatureResponse.signature),
                MultipartBody.Part.createFormData(
                    "expire",
                    signatureResponse.expire.toString()
                ),
                MultipartBody.Part.createFormData("token", signatureResponse.token),
                MultipartBody.Part.createFormData("fileName", fileName),
                MultipartBody.Part.createFormData(
                    "useUniqueFileName",
                    useUniqueFilename.toString()
                ),
                if (commaSeparatedTags != null) MultipartBody.Part.createFormData(
                    "tags",
                    commaSeparatedTags
                ) else null,
                if (folder != null) MultipartBody.Part.createFormData(
                    "folder",
                    folder
                ) else null,
                MultipartBody.Part.createFormData("isPrivateFile", isPrivateFile.toString()),
                if (customCoordinates != null) MultipartBody.Part.createFormData(
                    "customCoordinates",
                    customCoordinates
                ) else null,
                if (responseFields != null) MultipartBody.Part.createFormData(
                    "responseFields",
                    responseFields
                ) else null
            ).toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getCommaSeparatedTagsFromTags(tags: Array<String>?): String? {
        return tags?.joinToString { "\'$it\'" }
    }
}