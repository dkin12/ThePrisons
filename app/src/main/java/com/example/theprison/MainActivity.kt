package com.example.theprison

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.theprison.dataClass.SentimentAnalysisResponse
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var text = "자기 의지대로 살아가는 사람을 자유인이라 하네, 그는 강제도, 훼방도, 제한도 모르지.어떤 선택도 방해받지 않으며"
        callSentimentAnalysisApi(text) { sentiment ->
            Log.e("테스트", "$sentiment")
        }
    }
    private fun callSentimentAnalysisApi(text: String, callback: (String) -> Unit) {
        val url = "https://naveropenapi.apigw.ntruss.com/sentiment-analysis/v1/analyze"
        val client = OkHttpClient()
        val jsonBody = JSONObject().put("content", text)
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", "ku9gp1hhtv")
            .addHeader("X-NCP-APIGW-API-KEY", "afgqTq0FvoqpkNQZQG0psGh29WetKgQk3EDQtxow")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResult = response.body?.string()
                val responseObject = gson.fromJson(jsonResult, SentimentAnalysisResponse::class.java)
                Log.d("API Response", jsonResult ?: "")
                if (responseObject.document == null) {
                    Log.e("ERROR", "분석이 제대로 완료되지 않았습니다.")
                }
                var sentiment = ""
                if (responseObject.document != null) {
                    // 전체 문장 감정 정보 로그 출력
                    Log.e("MainActivity", "전체 문장 감정 : ${responseObject.document.sentiment}")
                    Log.e("MainActivity", "긍정 감정 확률 : ${responseObject.document.confidence.positive}")
                    Log.e("MainActivity", "중립 감정 확률 : ${responseObject.document.confidence.neutral}")
                    Log.e("MainActivity", "부전 감정 확률 : ${responseObject.document.confidence.negative}")
                    sentiment = responseObject.document.sentiment
                }
                if (responseObject.sentences != null) {
                    for (sentence in responseObject.sentences) {
                        if (sentence.sentiment != null) {
                            Log.d("MainActivity", "분류 문장: ${sentence.content}")
                            Log.d("MainActivity", "문장 감정: ${sentence.sentiment}")
                            Log.d("MainActivity", "중립 감정 확률: ${sentence.confidence.neutral}")
                            Log.d("MainActivity", "긍정 감정 확률: ${sentence.confidence.positive}")
                            Log.d("MainActivity", "부정 감정 확률: ${sentence.confidence.negative}")
                        }
                    }
                }
                // API 응답을 처리한 후 콜백으로 결과를 전달
                callback(sentiment)
            }

        })
    }

}
