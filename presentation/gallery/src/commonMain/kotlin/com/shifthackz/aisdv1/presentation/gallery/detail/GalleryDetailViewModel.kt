package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import kotlinx.coroutines.Job

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryIdsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageLikeUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.presentation.components.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Maintains detail navigation with bounded metadata prefetch and revision-fenced asynchronous reads.
 * Original bytes are loaded for explicit save/share/form-restoration actions, preserving page/cache identity.
 *
 * @param diagnostics Optional bounded observer for detail MVI transitions.
 * @param itemId Initial persistent identity, or nonpositive to open the last cached result.
 * @param dispatchersProvider Separates storage/native work from immediate UI publication.
 * @param buildInfoProvider Controls distribution-specific report visibility.
 * @param getGenerationResultUseCase Loads complete original content for explicit image actions or form restoration.
 * @param getAllGalleryIdsUseCase Reads gallery ordering without retaining original images.
 * @param getLastResultFromCacheUseCase Loads a transient generation when no persistent identity exists.
 * @param deleteGalleryItemUseCase Deletes the confirmed current persistent generation.
 * @param toggleImageVisibilityUseCase Changes the current generation's hidden state.
 * @param toggleImageLikeUseCase Changes the current generation's favorite state.
 * @param generationFormUpdateEvent Transfers captured parameters/source choice to a retained generation form.
 * @param router Handles detail dismissal, report navigation and form destinations.
 * @param platformActions Performs native original-image export, sharing and clipboard operations.
 * @param galleryReads Reads and observes current/neighbor metadata revisions.
 * @param contentCache Stores only the currently retained detail metadata window.
 * @param pagerBuffer Neighbor count requested on either side, clamped to zero or one.
 * @param onError Receives read, navigation, mutation and native-action failures.
 *
 * @author Dmitriy Moroz
 */
class GalleryDetailViewModel(
    diagnostics: UiDiagnostics,
    private val itemId: Long,
    private val dispatchersProvider: DispatchersProvider,
    private val buildInfoProvider: BuildInfoProvider,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    private val getAllGalleryIdsUseCase: GetAllGalleryIdsUseCase,
    private val getLastResultFromCacheUseCase: GetLastResultFromCacheUseCase,
    private val deleteGalleryItemUseCase: DeleteGalleryItemUseCase,
    private val toggleImageVisibilityUseCase: ToggleImageVisibilityUseCase,
    private val toggleImageLikeUseCase: ToggleImageLikeUseCase,
    private val generationFormUpdateEvent: GenerationFormUpdateEvent,
    private val router: GalleryDetailRouter,
    private val platformActions: GalleryDetailPlatformActions,
    private val galleryReads: GalleryReadGateway,
    private val contentCache: GalleryDetailContentCache,
    private val pagerBuffer: Int = GALLERY_DETAIL_PAGER_BUFFER,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<GalleryDetailState, GalleryDetailIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.GALLERY_DETAIL.asDiagnosticScreen(),
    initialState = GalleryDetailState(),
    effectDispatcher = dispatchersProvider.immediate,
) {


    private var currentItemId: Long = itemId
    private val safePagerBuffer = pagerBuffer.coerceIn(0, 1)
    private var galleryItemIds: List<Long> = emptyList()
    private var navigationRevision = 0L
    private var navigationJob: Job? = null
    private var prefetchJob: Job? = null
    private var metadataJob: Job? = null
    private var observedId: Long? = null

    init {
        load()
    }

    private val showReportButton: Boolean
        get() = buildInfoProvider.type != BuildType.FOSS

    override fun processIntent(intent: GalleryDetailIntent) {
        when (intent) {
            is GalleryDetailIntent.CopyToClipboard -> copyToClipboard(intent.content)
            GalleryDetailIntent.Delete.Request -> setActiveDialog(GalleryDetailDialog.DeleteConfirm)
            GalleryDetailIntent.Delete.Confirm -> delete()
            GalleryDetailIntent.DismissDialog -> setActiveDialog(GalleryDetailDialog.None)
            GalleryDetailIntent.Export.Image -> saveImage()
            GalleryDetailIntent.Export.Params -> shareParams()
            GalleryDetailIntent.NavigateBack -> router.navigateBack()
            GalleryDetailIntent.NavigatePrevious -> navigateToAdjacent(delta = -1)
            GalleryDetailIntent.NavigateNext -> navigateToAdjacent(delta = 1)
            is GalleryDetailIntent.NavigateToPage -> navigateToPage(intent.page)
            GalleryDetailIntent.Report -> currentState.content?.id?.let(router::navigateToReportImage)
            is GalleryDetailIntent.SelectTab -> updateState { it.copy(selectedTab = intent.tab) }
            GalleryDetailIntent.SendTo.Img2Img -> sendPromptToGenerationScreen(
                AiGenerationResult.Type.IMAGE_TO_IMAGE,
            )

            GalleryDetailIntent.SendTo.Txt2Img -> sendPromptToGenerationScreen(
                AiGenerationResult.Type.TEXT_TO_IMAGE,
            )

            GalleryDetailIntent.Share.Image -> shareImage()
            GalleryDetailIntent.Share.Params -> shareParams()
            GalleryDetailIntent.ToggleLike -> toggleLike()
            GalleryDetailIntent.ToggleVisibility -> toggleVisibility()
        }
    }

    private fun load() {
        val id = currentItemId
        val revision = ++navigationRevision
        navigationJob?.cancel()
        prefetchJob?.cancel()
        navigationJob = launch(dispatchersProvider.io) {
            runCatching {
                val ids = getGalleryItemIds()
                val result = getDisplayMetadata(id)
                ids to result
            }.onFailure { if (revision == navigationRevision) handleFailure(it) }
                .onSuccess { (ids, result) ->
                    withContext(dispatchersProvider.immediate) {
                        if (revision != navigationRevision || currentItemId != id) return@withContext
                        galleryItemIds = if (result.id in ids) ids else listOf(result.id)
                        val index = galleryItemIds.indexOf(result.id)
                        setCurrentContent(result, index, true)
                        prefetchPagerWindow(index)
                    }
                }
        }
    }

    private suspend fun getDisplayMetadata(id: Long): GalleryDetailContent =
        if (id <= 0) getLastResultFromCacheUseCase().also {
            if (it.image.length + it.inputImage.length > 32 * 1024 * 1024) throw IllegalStateException()
        }.toGalleryDetailContent(showReportButton)
        else galleryReads.metadata(listOf(id)).first().toGalleryDetailContent(showReportButton)

    private fun copyToClipboard(text: String) {
        launch(dispatchersProvider.io) {
            platformActions.copyText(text).handlePlatformResult()
        }
    }

    private fun saveImage() {
        val content = currentState.content ?: return
        val tab = currentState.selectedTab
        launch(dispatchersProvider.io) {
            val result = getGenerationResult(content.id)
            val original = if (tab == GalleryDetailTab.ORIGINAL) result.inputImage else result.image
            platformActions.saveImage(original).handlePlatformResult()
        }
    }

    private fun shareParams() {
        val text = currentState.content?.paramsText()?.takeIf(String::isNotEmpty) ?: return
        launch(dispatchersProvider.io) {
            platformActions.shareText(text).handlePlatformResult()
        }
    }

    private fun shareImage() {
        val content = currentState.content ?: return
        val tab = currentState.selectedTab
        launch(dispatchersProvider.io) {
            val result = getGenerationResult(content.id)
            val original = if (tab == GalleryDetailTab.ORIGINAL) result.inputImage else result.image
            platformActions.shareImage(original).handlePlatformResult()
        }
    }

    private fun delete() {
        setActiveDialog(GalleryDetailDialog.None)
        val id = currentState.content?.id ?: return
        val itemIndex = galleryItemIds.indexOf(id)
        if (itemIndex == -1) {
            launch(dispatchersProvider.io) {
                runCatching { deleteGalleryItemUseCase(id) }
                    .onFailure(::handleFailure)
                    .onSuccess {
                        withContext(dispatchersProvider.immediate) {
                            router.navigateBack()
                        }
                    }
            }
            return
        }

        val updatedIds = galleryItemIds.toMutableList().apply { removeAt(itemIndex) }
        contentCache.remove(id)
        galleryItemIds = updatedIds

        if (updatedIds.isEmpty()) {
            launch(dispatchersProvider.io) {
                runCatching { deleteGalleryItemUseCase(id) }
                    .onFailure(::handleFailure)
                    .onSuccess {
                        withContext(dispatchersProvider.immediate) {
                            updateState {
                                it.copy(
                                    content = null,
                                    pagerContents = emptyList(),
                                    galleryItemIds = emptyList(),
                                    tabs = emptyList(),
                                )
                            }
                            router.navigateBack()
                        }
                    }
            }
            return
        }

        val nextItemIndex = itemIndex.coerceAtMost(updatedIds.lastIndex)
        val nextItemId = updatedIds[nextItemIndex]
        currentItemId = nextItemId
        contentCache[nextItemId]
            ?.let { content ->
                setCurrentContent(
                    content = content,
                    itemIndex = nextItemIndex,
                    decodeMissing = false,
                )
                prefetchPagerWindow(nextItemIndex)
            }
            ?: loadPage(nextItemIndex, nextItemId)

        launch(dispatchersProvider.io) {
            runCatching { deleteGalleryItemUseCase(id) }
                .onFailure(::handleFailure)
        }
    }

    private fun sendPromptToGenerationScreen(screenType: AiGenerationResult.Type) {
        val selectedTab = currentState.selectedTab
        val id = currentState.content?.id ?: currentItemId
        launch(dispatchersProvider.io) {
            runCatching { getGenerationResult(id) }
                .onFailure(::handleFailure)
                .onSuccess { result ->
                    generationFormUpdateEvent.update(
                        generation = result,
                        route = screenType,
                        inputImage = selectedTab == GalleryDetailTab.ORIGINAL,
                    )
                    withContext(dispatchersProvider.immediate) {
                        when (screenType) {
                            AiGenerationResult.Type.TEXT_TO_IMAGE -> router.navigateToTextToImage()
                            AiGenerationResult.Type.IMAGE_TO_IMAGE -> router.navigateToImageToImage()
                        }
                    }
                }
        }
    }

    private fun toggleVisibility() {
        val id = currentState.content?.id ?: return
        val hidden = currentState.content?.hidden?.not() ?: return
        setHidden(id, hidden)
        launch(dispatchersProvider.io) {
            val result = runCatching { toggleImageVisibilityUseCase(id) }
            withContext(dispatchersProvider.immediate) {
                result
                    .onFailure {
                        setHidden(id, !hidden)
                        handleFailure(it)
                    }
                    .onSuccess { hidden ->
                        if (currentState.content?.hidden != hidden) {
                            setHidden(id, hidden)
                        }
                    }
            }
        }
    }

    private fun toggleLike() {
        val id = currentState.content?.id ?: return
        val liked = currentState.content?.liked?.not() ?: return
        setLiked(id, liked)
        launch(dispatchersProvider.io) {
            val result = runCatching { toggleImageLikeUseCase(id) }
            withContext(dispatchersProvider.immediate) {
                result
                    .onFailure {
                        setLiked(id, !liked)
                        handleFailure(it)
                    }
                    .onSuccess { liked ->
                        if (currentState.content?.liked != liked) {
                            setLiked(id, liked)
                        }
                    }
            }
        }
    }

    private fun setActiveDialog(dialog: GalleryDetailDialog) {
        updateState { it.copy(dialog = dialog) }
    }

    private fun navigateToAdjacent(delta: Int) {
        navigateToPage(currentState.pagerCurrentIndex + delta)
    }

    private fun navigateToPage(page: Int) {
        val targetId =
            galleryItemIds.getOrNull(page) ?: currentState.galleryItemIds.getOrNull(page) ?: return
        if (targetId == currentState.content?.id) return
        navigationRevision++
        navigationJob?.cancel()
        prefetchJob?.cancel()
        currentItemId = targetId
        loadPage(page, targetId)
    }

    private suspend fun GalleryDetailActionResult.handlePlatformResult() {
        when (this) {
            GalleryDetailActionResult.Done -> Unit
            GalleryDetailActionResult.Unsupported -> withContext(dispatchersProvider.immediate) {
                setActiveDialog(
                    GalleryDetailDialog.Error(Localization.string("error_generic")),
                )
            }

            is GalleryDetailActionResult.Failed -> withContext(dispatchersProvider.immediate) {
                setActiveDialog(GalleryDetailDialog.Error(message))
            }
        }
    }

    private fun handleFailure(t: Throwable) {
        if (t is CancellationException) throw t
        onError(t)
        updateState {
            it.copy(
                loading = false,
                dialog = GalleryDetailDialog.Error(
                    t.message?.takeIf(String::isNotBlank) ?: Localization.string("error_generic"),
                ),
            )
        }
    }

    private suspend fun getGenerationResult(id: Long): AiGenerationResult {
        if (id <= 0) return getLastResultFromCacheUseCase()
        return getGenerationResultUseCase(id)
    }

    private suspend fun getGalleryItemIds(): List<Long> =
        if (currentItemId <= 0) emptyList() else getAllGalleryIdsUseCase()

    private fun createPagerWindow(
        galleryItemIds: List<Long>,
        itemIndex: Int,
        content: GalleryDetailContent,
        decodeMissing: Boolean,
    ): GalleryDetailPagerWindow {
        if (itemIndex == -1 || galleryItemIds.isEmpty()) {
            return GalleryDetailPagerWindow(
                contents = listOf(content),
                startIndex = 0,
                currentIndex = 0,
            )
        }

        val startIndex = (itemIndex - safePagerBuffer).coerceAtLeast(0)
        val endIndex = (itemIndex + safePagerBuffer + 1).coerceAtMost(galleryItemIds.size)
        if (decodeMissing) {
            contentCache[content.id] = content
            return GalleryDetailPagerWindow(
                contents = listOf(content),
                startIndex = itemIndex,
                currentIndex = itemIndex,
            )
        }

        contentCache[content.id] = content
        var cachedStartIndex = itemIndex
        while (
            cachedStartIndex > startIndex &&
            contentCache.containsKey(galleryItemIds[cachedStartIndex - 1])
        ) {
            cachedStartIndex -= 1
        }

        var cachedEndIndex = itemIndex + 1
        while (
            cachedEndIndex < endIndex &&
            contentCache.containsKey(galleryItemIds[cachedEndIndex])
        ) {
            cachedEndIndex += 1
        }

        return GalleryDetailPagerWindow(
            contents = galleryItemIds
                .subList(cachedStartIndex, cachedEndIndex)
                .mapNotNull(contentCache::get),
            startIndex = cachedStartIndex,
            currentIndex = itemIndex,
        )
    }

    private fun List<GalleryDetailContent>.updateContent(
        id: Long,
        update: (GalleryDetailContent) -> GalleryDetailContent,
    ): List<GalleryDetailContent> = map { content ->
        if (content.id == id) update(content) else content
    }

    private fun setCurrentContent(
        content: GalleryDetailContent,
        itemIndex: Int,
        decodeMissing: Boolean,
    ) {
        val keep = galleryItemIds.subList(
            (itemIndex - 1).coerceAtLeast(0),
            (itemIndex + 2).coerceIn(0, galleryItemIds.size)
        ).toSet() + content.id
        contentCache.retain(keep)
        observeCurrentMetadata(content.id)
        val tabs = GalleryDetailTab.consume(content.generationType)
        val selectedTab = currentState.selectedTab.takeIf(tabs::contains) ?: tabs.first()
        val pagerWindow = createPagerWindow(
            galleryItemIds = galleryItemIds,
            itemIndex = itemIndex,
            content = content,
            decodeMissing = decodeMissing,
        )
        updateState {
            it.copy(
                loading = false,
                tabs = tabs,
                selectedTab = selectedTab,
                galleryItemIds = galleryItemIds,
                content = content,
                pagerContents = pagerWindow.contents,
                pagerContentStartIndex = pagerWindow.startIndex,
                pagerCurrentIndex = pagerWindow.currentIndex,
            )
        }
    }

    private fun observeCurrentMetadata(id: Long) {
        if (observedId == id) return
        observedId = id
        metadataJob?.cancel()
        if (id <= 0) return
        metadataJob = launch(dispatchersProvider.io) {
            galleryReads.observeItem(id).collect { row ->
                withContext(dispatchersProvider.immediate) {
                    if (currentItemId != id) return@withContext
                    if (row == null) {
                        contentCache.remove(id)
                        router.navigateBack()
                    } else {
                        val refreshed = row.toGalleryDetailContent(showReportButton)
                        if (currentState.content != refreshed) {
                            contentCache[id] = refreshed
                            setCurrentContent(refreshed, galleryItemIds.indexOf(id), false)
                        }
                    }
                }
            }
        }
    }

    private fun prefetchPagerWindow(itemIndex: Int) {
        prefetchJob?.cancel()
        if (itemIndex < 0 || galleryItemIds.isEmpty()) return
        val revision = navigationRevision
        val snapshot = galleryItemIds
        val currentId = currentItemId
        val ids = snapshot.subList(
            (itemIndex - safePagerBuffer).coerceAtLeast(0),
            (itemIndex + safePagerBuffer + 1).coerceAtMost(snapshot.size)
        )
        prefetchJob = launch(dispatchersProvider.io) {
            runCatching { galleryReads.metadata(ids) }.onSuccess { rows ->
                withContext(dispatchersProvider.immediate) {
                    if (revision != navigationRevision || currentId != currentItemId || snapshot != galleryItemIds) return@withContext
                    rows.filter { it.id != currentId }.forEach {
                        contentCache[it.id] = it.toGalleryDetailContent(showReportButton)
                    }
                    contentCache[currentId]?.let { setCurrentContent(it, itemIndex, false) }
                }
            }.onFailure { if (it is CancellationException) throw it else onError(it) }
        }
    }

    private fun setHidden(id: Long, hidden: Boolean) {
        updateCachedContent(id) { content -> content.copy(hidden = hidden) }
    }

    private fun setLiked(id: Long, liked: Boolean) {
        updateCachedContent(id) { content -> content.copy(liked = liked) }
    }

    private fun updateCachedContent(
        id: Long,
        update: (GalleryDetailContent) -> GalleryDetailContent,
    ) {
        contentCache[id] = contentCache[id]?.let(update) ?: return
        updateState { state ->
            state.copy(
                content = state.content?.let { content ->
                    if (content.id == id) update(content) else content
                },
                pagerContents = state.pagerContents.updateContent(
                    id = id,
                    update = update,
                ),
            )
        }
    }

    private fun loadPage(page: Int, id: Long) {
        val revision = ++navigationRevision
        navigationJob?.cancel()
        prefetchJob?.cancel()
        navigationJob = launch(dispatchersProvider.io) {
            runCatching { getDisplayMetadata(id) }
                .onFailure { if (revision == navigationRevision) handleFailure(it) }
                .onSuccess { result ->
                    withContext(dispatchersProvider.immediate) {
                        if (revision != navigationRevision || currentItemId != id) return@withContext
                        val index = galleryItemIds.indexOf(id).takeIf { it >= 0 } ?: page
                        setCurrentContent(result, index, false)
                        prefetchPagerWindow(index)
                    }
                }
        }
    }

    private data class GalleryDetailPagerWindow(
        val contents: List<GalleryDetailContent>,
        val startIndex: Int,
        val currentIndex: Int,
    )
}

internal const val GALLERY_DETAIL_PAGER_BUFFER = 3
