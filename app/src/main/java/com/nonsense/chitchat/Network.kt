package com.nonsense.chitchat

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface RequestService {
    @FormUrlEncoded
    @POST(value = "/chat")
    fun chat(
        @Field("message") text: String,
    ): Call<String>
}

class Network {
    companion object {
        private var oldIp = "192.168.43.194"
        private var oldPort = "5000"

        //创建拦截器
        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url()
            val builder = url.newBuilder()
            requestBuilder.url(builder.build())
                .method(request.method(), request.body())
                .addHeader("clientType", "ANDROID")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
            chain.proceed(requestBuilder.build())
        }

        //创建OKhttp
        private val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)


        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://$oldIp:$oldPort")
            .addConverterFactory(LenientGsonConverterFactory.create())
            .client(client.build())
            .build()

        var service: RequestService = retrofit.create(RequestService::class.java)

        fun update(ip: String, port: String) {
            if (ip == oldIp && port == oldPort) {
                return
            }
            if (ip != oldIp) {
                oldIp = ip
            }
            if (port != oldPort) {
                oldPort = port
            }
            retrofit = Retrofit.Builder()
                .baseUrl("http://$oldIp:$oldPort")
                .addConverterFactory(LenientGsonConverterFactory.create())
                .client(client.build())
                .build()
            service = retrofit.create(RequestService::class.java)
        }
    }
}


