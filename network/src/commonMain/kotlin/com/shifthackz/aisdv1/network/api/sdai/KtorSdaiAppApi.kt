package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.api.sdai.model.BlogResponse
import com.shifthackz.aisdv1.network.api.sdai.model.FaqResponse
import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.model.SupporterRaw
import com.shifthackz.aisdv1.network.request.ReportRequest
import com.shifthackz.aisdv1.network.response.DownloadableModelResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import com.shifthackz.aisdv1.network.client.boundedGet
import com.shifthackz.aisdv1.network.client.boundedPost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom

/**
 * Ktor implementation of SDAI app metadata, model catalog, and report calls.
 *
 * @param httpClient Configured Ktor client used to send SDAI service requests.
 * @param appBaseUrl Base URL for SDAI app metadata and model catalogs.
 * @param reportBaseUrl Base URL for problem report submissions.
 *
 * @author Dmitriy Moroz
 */
class KtorSdaiAppApi(
    private val httpClient: HttpClient,
    private val appBaseUrl: String,
    private val reportBaseUrl: String,
) : SdaiAppApi {

    /**
     * Creates a typed metadata client with the standard application HTTP policies.
     *
     * @param appBaseUrl Root URL of application metadata and model catalog endpoints.
     * @param reportBaseUrl Root URL used to submit user problem reports.
     *
     * @author Dmitriy Moroz
     */
    constructor(
        appBaseUrl: String,
        reportBaseUrl: String,
    ) : this(
        httpClient = createConfiguredHttpClient(),
        appBaseUrl = appBaseUrl,
        reportBaseUrl = reportBaseUrl,
    )

    /**
     * Fetches the current supporter list from the metadata service; HTTP and decoding failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchSupporters(): List<SupporterRaw> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_SUPPORTERS)
        }
        .body()

    override suspend fun fetchFaq(): FaqResponse = httpClient.boundedGet {
        url.takeFrom(appBaseUrl)
        url.appendPathSegments("faq.json")
        timeout { requestTimeoutMillis = WEBSITE_CONTENT_TIMEOUT_MILLIS }
        trackUsage(NetworkUsageCategory.CONFIGS)
    }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    override suspend fun fetchBlog(): BlogResponse = httpClient.boundedGet {
        url.takeFrom(appBaseUrl)
        url.appendPathSegments("blog.json")
        timeout { requestTimeoutMillis = WEBSITE_CONTENT_TIMEOUT_MILLIS }
        trackUsage(NetworkUsageCategory.CONFIGS)
    }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches the typed MNN catalog without interpreting an HTTP failure as an empty catalog.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchMnnModels(): List<DownloadableModelResponse> = httpClient.boundedGet {
        url.takeFrom(appBaseUrl)
        url.appendPathSegments("mnn.json")
        trackUsage(NetworkUsageCategory.CONFIGS)
    }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Downloads the dedicated ESRGAN checkpoint list and records configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchUpscalerEsrganModels(): List<DownloadableModelResponse> =
        httpClient.boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments("upscaler-esrgan.json")
            trackUsage(NetworkUsageCategory.CONFIGS)
        }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Downloads the dedicated MNN super-resolution list and records configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchUpscalerMnnModels(): List<DownloadableModelResponse> =
        httpClient.boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments("upscaler-mnn.json")
            trackUsage(NetworkUsageCategory.CONFIGS)
        }.trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches ONNX catalog metadata and accounts response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchOnnxModels(): List<DownloadableModelResponse> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_MODELS)
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches MediaPipe download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchMediaPipeModels(): List<DownloadableModelResponse> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_MEDIA_PIPE)
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches stable-diffusion.cpp download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchSdxlModels(): List<DownloadableModelResponse> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_SDXL)
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches Core ML download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchCoreMlModels(): List<DownloadableModelResponse> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_CORE_ML)
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Fetches Bonsai download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchBonsaiModels(): List<DownloadableModelResponse> = httpClient
        .boundedGet {
            url.takeFrom(appBaseUrl)
            url.appendPathSegments(PATH_BONSAI)
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        .trackedJsonBody(NetworkUsageCategory.CONFIGS)

    /**
     * Submits the typed user problem report; unsuccessful HTTP responses remain failures.
     *
     * @param request Typed user-submitted problem report body sent to the report service.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun postReport(request: ReportRequest) {
        httpClient.boundedPost {
            url.takeFrom(reportBaseUrl)
            url.appendPathSegments(PATH_REPORT)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    /**
     * Stable metadata endpoint filenames shared by these typed HTTP operations.
     *
     * @author Dmitriy Moroz
     */
    private companion object {
        const val WEBSITE_CONTENT_TIMEOUT_MILLIS = 8_000L
        const val PATH_SUPPORTERS = "supporters.json"
        const val PATH_MODELS = "models.json"
        const val PATH_MEDIA_PIPE = "mediapipe.json"
        const val PATH_SDXL = "sdxl.json"
        const val PATH_CORE_ML = "coreml.json"
        const val PATH_BONSAI = "bonsai.json"
        const val PATH_REPORT = "report"
    }
}
