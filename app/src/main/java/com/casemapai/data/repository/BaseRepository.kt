package com.casemapai.data.repository

import retrofit2.Response
import timber.log.Timber
import java.io.IOException

open class BaseRepository{

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {

        val result : Result<T> = safeApiResult(call,errorMessage)
        var data : T? = null

        when(result) {
            is Result.Success -> {
                data = result.data
            }

            is Result.Error -> {
                Timber.d("$errorMessage & Exception - ${result.exception}")
            }
        }

        return data

    }

    private suspend fun <T: Any> safeApiResult(call: suspend ()-> Response<T>, errorMessage: String) : Result<T>{
        val response = call.invoke()
        if(response.isSuccessful){
            val responseData = response.body()

            responseData?.run{
                return Result.Success(this)
            }
        }

        return Result.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}