package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import kotlinx.coroutines.flow.Flow

/**
 * Separates remote catalog transfers from local metadata, download completeness and selected-model observation.
 *
 * @author Dmitriy Moroz
 */
sealed interface DownloadableModelDataSource {

    /**
     * Remote catalog HTTP and archive-download operations; transport and decoding failures remain observable.
     *
     * @author Dmitriy Moroz
     */
    interface Remote : DownloadableModelDataSource {
        /**
         * Fetches exactly one provider catalog; HTTP, decoding and cancellation failures propagate.
         *
         * @param type Runtime family identifying the catalog endpoint.
         *
         * @author Dmitriy Moroz
         */
        suspend fun fetchCatalog(type: LocalAiModel.Type): List<LocalAiModel>

        /**
         * Streams the selected catalog source and publishes completion only after its complete model payload is activated.
         *
         * @param id identifier of the target entity.
         * @param url remote URL used by the operation.
         *
         * @author Dmitriy Moroz
         */
        fun download(id: String, url: String): Flow<DownloadState>
    }

    /**
     * Cached catalog metadata with current filesystem completeness and independent provider selection.
     *
     * @author Dmitriy Moroz
     */
    interface Local : DownloadableModelDataSource {
        /**
         * Reads dedicated ESRGAN metadata and current app-owned download state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllUpscalers(): List<LocalAiModel>

        /**
         * Resolves the complete owned checkpoint for a downloaded catalog model.
         *
         * @param model Downloaded catalog entry; custom source paths are not accepted.
         * @author Dmitriy Moroz
         */
        fun getModelPath(model: LocalAiModel): String

        /**
         * Reads ONNX catalog models with current downloaded and selected state, including the custom entry outside Play.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllOnnx(): List<LocalAiModel>

        /**
         * Reads cached MNN models with current completion and selection state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllMnn(): List<LocalAiModel>

        /**
         * Observes cached MNN metadata combined with selection changes.
         *
         * @author Dmitriy Moroz
         */
        fun observeAllMnn(): Flow<List<LocalAiModel>>

        /**
         * Reads available MediaPipe model metadata with current local download and selection state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllMediaPipe(): List<LocalAiModel>

        /**
         * Reads stable-diffusion.cpp model metadata with current local download and selection state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllSdxl(): List<LocalAiModel>

        /**
         * Reads Core ML model metadata with current local download and selection state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllCoreMl(): List<LocalAiModel>

        /**
         * Reads Bonsai model metadata with current local download and selection state.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getAllBonsai(): List<LocalAiModel>

        /**
         * Resolves a catalog or supported custom identity and attaches its current file and selection state; missing identities fail.
         *
         * @param id identifier of the target entity.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getById(id: String): LocalAiModel

        /**
         * Resolves the currently selected ONNX identity; absent metadata remains a failure.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getSelectedOnnx(): LocalAiModel

        /**
         * Resolves the currently selected Core ML identity; absent metadata remains a failure.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getSelectedCoreMl(): LocalAiModel

        /**
         * Resolves the currently selected Bonsai identity; absent metadata remains a failure.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getSelectedBonsai(): LocalAiModel

        /**
         * Resolves the currently selected stable-diffusion.cpp identity; absent metadata remains a failure.
         *
         * @author Dmitriy Moroz
         */
        suspend fun getSelectedSdxl(): LocalAiModel

        /**
         * Observes cached ONNX models combined with current selection and completion state.
         *
         * @author Dmitriy Moroz
         */
        fun observeAllOnnx(): Flow<List<LocalAiModel>>

        /**
         * Observes cached stable-diffusion.cpp models combined with current selection and completion state.
         *
         * @author Dmitriy Moroz
         */
        fun observeAllSdxl(): Flow<List<LocalAiModel>>

        /**
         * Observes cached Core ML models combined with current selection and completion state.
         *
         * @author Dmitriy Moroz
         */
        fun observeAllCoreMl(): Flow<List<LocalAiModel>>

        /**
         * Observes cached Bonsai models combined with current selection and completion state.
         *
         * @author Dmitriy Moroz
         */
        fun observeAllBonsai(): Flow<List<LocalAiModel>>

        /**
         * Upserts catalog metadata without persisting synthetic custom-model entries.
         *
         * @param list Fetched provider catalog metadata to upsert; synthetic custom entries are not persisted.
         *
         * @author Dmitriy Moroz
         */
        suspend fun save(list: List<LocalAiModel>)

        /**
         * Removes only the owned model payload; MNN removal waits for active inference leases.
         *
         * @param id identifier of the target entity.
         *
         * @author Dmitriy Moroz
         */
        suspend fun delete(id: String)
    }
}
