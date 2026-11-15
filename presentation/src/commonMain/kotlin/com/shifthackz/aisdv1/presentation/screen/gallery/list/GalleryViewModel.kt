package com.shifthackz.aisdv1.presentation.screen.gallery.list

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.domain.entity.BackgroundWorkResult
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsLikedUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsVisibilityUseCase
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.presentation.widget.image.GalleryMemoryBudget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Owns stable gallery positions, a bounded metadata window and selection across window eviction.
 *
 * @param galleryReads Payload-free catalog and metadata reads.
 * @param memoryBudget Explicit metadata window and prefetch limits.
 *
 * @author Dmitriy Moroz
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModel(
    diagnostics: UiDiagnostics,
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    /**
     * Exposes the `getMediaStoreInfoUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val getMediaStoreInfoUseCase: GetMediaStoreInfoUseCase,
    /**
     * Exposes the `backgroundWorkObserver` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val backgroundWorkObserver: BackgroundWorkObserver,
    /**
     * Exposes the `preferenceManager` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    /**
     * Exposes the `deleteAllGalleryUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val deleteAllGalleryUseCase: DeleteAllGalleryUseCase,
    /**
     * Exposes the `deleteGalleryItemsUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val deleteGalleryItemsUseCase: DeleteGalleryItemsUseCase,
    /**
     * Exposes the `setGalleryItemsVisibilityUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val setGalleryItemsVisibilityUseCase: SetGalleryItemsVisibilityUseCase,
    /**
     * Exposes the `setGalleryItemsLikedUseCase` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val setGalleryItemsLikedUseCase: SetGalleryItemsLikedUseCase,
    /**
     * Exposes the `galleryExportService` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val galleryExportService: GalleryExportService,
    /**
     * Exposes the `galleryRouter` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val galleryRouter: GalleryRouter,
    private val galleryReads: GalleryReadGateway,
    private val memoryBudget: GalleryMemoryBudget,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<GalleryState, GalleryIntent, GalleryEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.GALLERY.asDiagnosticScreen(),
    initialState = GalleryState(grid = preferenceManager.galleryGrid),
    effectDispatcher = dispatchersProvider.immediate,
) {
    private val windowOffset = MutableStateFlow(0)

    init {
        observePreferences()
        observeBackgroundWork()
        observeGallery()
        loadMediaStoreInfo()
    }

    override fun processIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.Viewport -> windowOffset.value =
                (intent.first - memoryBudget.metadataPrefetch).coerceAtLeast(0)

            GalleryIntent.DismissDialog -> setActiveDialog(GalleryDialog.None)

            GalleryIntent.Export.All.Request -> setActiveDialog(GalleryDialog.ConfirmExport(true))

            GalleryIntent.Export.All.Confirm -> launchGalleryExport(exportAll = true)

            GalleryIntent.Export.Selection.Request -> setActiveDialog(
                GalleryDialog.ConfirmExport(
                    false
                )
            )

            GalleryIntent.Export.Selection.Confirm -> launchGalleryExport(exportAll = false)

            is GalleryIntent.OpenItem -> galleryRouter.navigateToGalleryDetails(intent.id)

            GalleryIntent.OpenDrawer -> galleryRouter.openDrawer()

            GalleryIntent.OpenMediaStoreFolder -> currentState.mediaStoreInfo.folderUri
                ?.let { emitEffect(GalleryEffect.OpenMediaStoreFolder(it)) }

            is GalleryIntent.ChangeSelectionMode -> updateState {
                it.copy(
                    selectionMode = intent.flag,
                    selection = if (!intent.flag) emptyList() else it.selection,
                    selectedFlags = if (!intent.flag) emptyList() else it.selectedFlags,
                )
            }

            is GalleryIntent.ToggleItemSelection -> {
                updateState {
                    val newSelection = it.selection.toMutableList()
                    if (intent.id in newSelection) newSelection.remove(intent.id)
                    else newSelection.add(intent.id)
                    it.copy(selection = newSelection)
                }; refreshSelection()
            }

            GalleryIntent.Delete.Selection.Request -> setActiveDialog(GalleryDialog.DeleteSelectionConfirm)

            GalleryIntent.Delete.Selection.Confirm -> launchDeletion {
                deleteGalleryItemsUseCase(currentState.selection)
            }

            GalleryIntent.Delete.All.Request -> setActiveDialog(GalleryDialog.DeleteAllConfirm)

            GalleryIntent.Delete.All.Confirm -> launchDeletion {
                deleteAllGalleryUseCase()
            }

            GalleryIntent.UnselectAll -> updateState {
                it.copy(selection = emptyList())
            }

            GalleryIntent.ToggleSelectionVisibility -> launchSelectionVisibilityToggle()

            GalleryIntent.ToggleSelectionLike -> launchSelectionLikeToggle()

            GalleryIntent.Dropdown.Toggle -> updateState {
                it.copy(dropdownMenuShow = !it.dropdownMenuShow)
            }

            GalleryIntent.Dropdown.Show -> updateState {
                it.copy(dropdownMenuShow = true)
            }

            GalleryIntent.Dropdown.Close -> updateState {
                it.copy(dropdownMenuShow = false)
            }
        }
    }

    private fun observePreferences() {
        launch(dispatchersProvider.immediate) {
            preferenceManager
                .observe()
                .catch { onError(it) }
                .collect { settings ->
                    updateState { it.copy(grid = settings.galleryGrid) }
                }
        }
    }

    private fun observeBackgroundWork() {
        launch(dispatchersProvider.immediate) {
            backgroundWorkObserver
                .observeResult()
                .filterIsInstance<BackgroundWorkResult.Success>()
                .catch { onError(it) }
                .collect { loadMediaStoreInfo() }
        }
    }

    private fun observeGallery() {
        launch(dispatchersProvider.io) {
            galleryReads.observeIds().catch { onError(it) }.collect { ids ->
                val existing = ids.toHashSet()
                updateState {
                    it.copy(
                        itemIds = ids,
                        selection = it.selection.filter(existing::contains)
                    )
                }
                refreshSelection()
            }
        }
        launch(dispatchersProvider.io) {
            windowOffset.flatMapLatest { offset ->
                galleryReads.observeMetadata(
                    memoryBudget.metadataWindow,
                    offset
                )
            }
                .catch { t ->
                    if (t is CancellationException) throw t
                    onError(t)
                    updateState {
                        it.copy(
                            loading = false,
                            dialog = GalleryDialog.Error(Localization.string("error_generic"))
                        )
                    }
                }.collect { results ->
                    updateState {
                        it.copy(
                            loading = false,
                            items = results.map { row ->
                                GalleryGridItemUi(
                                    id = row.id,
                                    hidden = row.isHidden,
                                    liked = row.isLiked,
                                    imageRevision = row.imageRevision
                                )
                            })
                    }
                    refreshSelection()
                }
        }
    }

    private fun refreshSelection() {
        val ids = currentState.selection
        launch(dispatchersProvider.io) {
            runCatching { galleryReads.flags(ids) }.onSuccess { rows ->
                updateState { state ->
                    if (state.selection != ids) state else state.copy(selectedFlags = rows.map { row ->
                        GalleryGridItemUi(
                            id = row.id,
                            hidden = row.hidden,
                            liked = row.liked,
                            imageRevision = row.imageRevision
                        )
                    })
                }
            }.onFailure { if (it is CancellationException) throw it else onError(it) }
        }
    }

    private fun loadMediaStoreInfo() {
        launch(dispatchersProvider.io) {
            runCatching { getMediaStoreInfoUseCase() }
                .onFailure(onError)
                .onSuccess { info ->
                    updateState { it.copy(mediaStoreInfo = info) }
                }
        }
    }

    private fun launchDeletion(action: suspend () -> Unit) {
        setActiveDialog(GalleryDialog.None)
        launch(dispatchersProvider.io) {
            runCatching { action() }
                .onFailure { t ->
                    onError(t)
                    setActiveDialog(
                        GalleryDialog.Error(
                            t.message?.takeIf(String::isNotBlank)
                                ?: Localization.string("error_generic")
                        ),
                    )
                }
                .onSuccess {
                    updateState {
                        it.copy(
                            selectionMode = false,
                            selection = emptyList(),
                            selectedFlags = emptyList(),
                        )
                    }
                    refreshGallery()
                }
        }
    }

    private fun launchSelectionVisibilityToggle() {
        val state = currentState
        val ids = state.selection
        if (ids.isEmpty()) return
        launch(dispatchersProvider.io) {
            runCatching {
                val shouldHide = galleryReads.flags(ids).any { !it.hidden }
                setGalleryItemsVisibilityUseCase(ids, shouldHide)
            }
                .onFailure { t ->
                    onError(t)
                    setActiveDialog(
                        GalleryDialog.Error(
                            t.message?.takeIf(String::isNotBlank)
                                ?: Localization.string("error_generic")
                        ),
                    )
                }
                .onSuccess {
                    updateState {
                        it.copy(
                            selectionMode = false,
                            selection = emptyList(),
                            selectedFlags = emptyList(),
                        )
                    }
                    refreshGallery()
                }
        }
    }

    private fun launchSelectionLikeToggle() {
        val state = currentState
        val ids = state.selection
        if (ids.isEmpty()) return
        launch(dispatchersProvider.io) {
            runCatching {
                val shouldLike = galleryReads.flags(ids).any { !it.liked }
                setGalleryItemsLikedUseCase(ids, shouldLike)
            }
                .onFailure { t ->
                    onError(t)
                    setActiveDialog(
                        GalleryDialog.Error(
                            t.message?.takeIf(String::isNotBlank)
                                ?: Localization.string("error_generic")
                        ),
                    )
                }
                .onSuccess {
                    updateState {
                        it.copy(
                            selectionMode = false,
                            selection = emptyList(),
                            selectedFlags = emptyList(),
                        )
                    }
                    refreshGallery()
                }
        }
    }

    private fun launchGalleryExport(exportAll: Boolean) {
        setActiveDialog(GalleryDialog.ExportInProgress)
        launch(dispatchersProvider.io) {
            runCatching {
                galleryExportService.export(if (exportAll) null else currentState.selection)
            }
                .onFailure { t ->
                    onError(t)
                    setActiveDialog(
                        GalleryDialog.Error(
                            t.message?.takeIf(String::isNotBlank)
                                ?: Localization.string("error_generic")
                        ),
                    )
                }
                .onSuccess { result ->
                    setActiveDialog(GalleryDialog.None)
                    emitEffect(GalleryEffect.ShareExport(result.filePath))
                }
        }
    }

    private fun refreshGallery() {
        loadMediaStoreInfo()
    }

    private fun setActiveDialog(dialog: GalleryDialog) = updateState {
        it.copy(dialog = dialog, dropdownMenuShow = false)
    }
}
