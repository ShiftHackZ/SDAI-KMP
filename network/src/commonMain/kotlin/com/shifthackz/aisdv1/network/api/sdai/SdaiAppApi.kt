package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.model.SupporterRaw
import com.shifthackz.aisdv1.network.request.ReportRequest
import com.shifthackz.aisdv1.network.response.DownloadableModelResponse

/**
 * Typed HTTP access to application metadata, model catalogs and user problem reports.
 *
 * @author Dmitriy Moroz
 */
interface SdaiAppApi {

    /**
     * Fetches the current supporter list from the metadata service; HTTP and decoding failures propagate.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchSupporters(): List<SupporterRaw>

    /**
     * Fetches ONNX download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchOnnxModels(): List<DownloadableModelResponse>

    /**
     * Fetches the typed MNN catalog without interpreting an HTTP failure as an empty catalog.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchMnnModels(): List<DownloadableModelResponse>

    /**
     * Fetches MediaPipe download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchMediaPipeModels(): List<DownloadableModelResponse>

    /**
     * Fetches stable-diffusion.cpp download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchSdxlModels(): List<DownloadableModelResponse>

    /**
     * Fetches Core ML download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchCoreMlModels(): List<DownloadableModelResponse>

    /**
     * Fetches Bonsai download metadata and accounts its response bytes as configuration traffic.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchBonsaiModels(): List<DownloadableModelResponse>

    /**
     * Submits the typed user problem report; unsuccessful HTTP responses remain failures.
     *
     * @param request Typed user-submitted problem report body sent to the report service.
     *
     * @author Dmitriy Moroz
     */
    suspend fun postReport(request: ReportRequest)
}
