package com.testndk.jnistudy.ui.activity

import com.testndk.jnistudy.R
import com.testndk.jnistudy.bean.KtorTestModel
import com.testndk.jnistudy.utils.LogUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.http
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.ANDROID
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.host
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.android.synthetic.main.activity_ktor.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy

class KtorActivity : BaseActivity() {
    override fun initLayout() = R.layout.activity_ktor

    override fun initView() {
        super.initView()
        runBlocking {
            val client = HttpClient(OkHttp) {
                defaultRequest {
                    host = "api.apiopen.top"
                    header("user", "1111")
                    header("user1", "1111")
                    header("user2", "1111")
                    header("user3", "1111")
                }
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = LogLevel.ALL
                }
                install(JsonFeature) {
                    serializer = GsonSerializer {
                        // .GsonBuilder
                        serializeNulls()
                        disableHtmlEscaping()
                    }
                }
            }
//            val client = HttpClient(Android) {
//                defaultRequest {
//                    host = "api.apiopen.top"
//                    header("user", "1111")
//                    header("user1", "1111")
//                    header("user2", "1111")
//                    header("user3", "1111")
//                }
//                engine {
//                    connectTimeout = 100_000
//                }
//                install(Logging) {
//                    logger = Logger.SIMPLE
//                    level = LogLevel.ALL
//                }
//                install(JsonFeature) {
//                    serializer = GsonSerializer {
//                        // .GsonBuilder
//                        serializeNulls()
//                        disableHtmlEscaping()
//                    }
//                }
//            }
            client.use {
                val ktorResult = it.get<KtorTestModel> {
                    url {
                        encodedPath = "/getJoke?page=1&count=2&type=video"
                    }
                    headers {
                        append("data", "111")
                        append("name", "222")
                        append("age", "333")
                    }
                    contentType(ContentType.Application.Json)
                }
                tvResult.text = ktorResult.toString()
                LogUtils.eLog(ktorResult)
            }
        }
    }
}
