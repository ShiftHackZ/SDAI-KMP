package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mappers.mapRawToCheckpointDomain
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi

/**
 * Fetches typed catalog metadata and delegates archive transfers to the platform downloader.
 *
 * @param api Typed application metadata HTTP client.
 * @param fileDownloader Platform archive downloader and progress source.
 *
 * @author Dmitriy Moroz
 */
internal class DownloadableModelRemoteDataSource(
    private val api: SdaiAppApi,
    private val fileDownloader: DownloadableModelFileDownloader,
) : DownloadableModelDataSource.Remote {

    /**
     * Fetches one typed endpoint without applying offline recovery or fetching unrelated providers.
     *
     * @param type Runtime family identifying the catalog endpoint and domain mapping.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchCatalog(type: LocalAiModel.Type): List<LocalAiModel> = when (type) {
        LocalAiModel.Type.ONNX -> api.fetchOnnxModels()
        LocalAiModel.Type.MediaPipe -> api.fetchMediaPipeModels()
        LocalAiModel.Type.Sdxl -> api.fetchSdxlModels()
        LocalAiModel.Type.CoreMl -> api.fetchCoreMlModels()
        LocalAiModel.Type.Bonsai -> api.fetchBonsaiModels()
        LocalAiModel.Type.Mnn -> api.fetchMnnModels()
    }.mapRawToCheckpointDomain(type)

    /**
     * Streams the selected catalog source and publishes completion only after its complete model payload is activated.
     *
     * @param id identifier of the target entity.
     * @param url remote URL used by the operation.
     *
     * @author Dmitriy Moroz
     */
    override fun download(id: String, url: String) = fileDownloader.download(id, url)
}
