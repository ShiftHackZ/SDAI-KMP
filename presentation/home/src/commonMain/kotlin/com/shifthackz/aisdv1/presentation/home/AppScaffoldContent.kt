package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.isHomeRoute
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityState
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityWidget
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalFloatingNavigationBarInset
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformRotationEffect
import kotlinx.coroutines.launch
import com.shifthackz.aisdv1.presentation.components.widget.navigation.isPlatformNavigationBarFloating
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarObscured
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative

/**
 * Renders the home drawer, persistent tabs and connection badge from caller-owned state.
 * Route content remains supplied by the application host; no settings or connectivity are observed here.
 * Platform inset helpers reserve safe space only where the host window has not already consumed it.
 * Native floating tabs keep the route body underneath them while exposing their height through a local.
 * Expanded Android windows use a rail; system-owned iOS tabs report their inset through the native host.
 * Native bottom reservations apply to the page body so the drawer still covers the complete window.
 * Native windows dismiss an open drawer when rotating, avoiding a partially translated old layout.
 *
 * @param currentRoute Active route determining destination selection and chrome visibility.
 * @param settings Provider and developer-mode flags determining the drawer destinations.
 * @param buildInfoProvider Build label and platform used by drawer branding and provider names.
 * @param connectivityContent Supplies connection state around the route body, preserving observer scope.
 * @param processAction Receives destination selections without owning navigation history.
 * @param modifier Placement of the complete drawer and scaffold.
 * @param drawerState Caller-owned drawer position, including its initial open or closed value.
 * @param shouldShowHomeChrome Whether this retained shell displays home navigation and connection status.
 * @param content Route renderer receiving the available body modifier.
 * @author Dmitriy Moroz
 */
@Composable
internal fun AppScaffoldContent(
    currentRoute: AppRoute,
    settings: Settings,
    buildInfoProvider: BuildInfoProvider,
    connectivityContent: @Composable (@Composable (ConnectivityState) -> Unit) -> Unit,
    processAction: (HomeNavigationAction) -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    shouldShowHomeChrome: Boolean = currentRoute.isHomeRoute(),
    content: @Composable (Modifier) -> Unit,
) {
    var drawerWidth by remember { mutableIntStateOf(0) }
    val shouldShowHomeNavigation = shouldShowHomeChrome && currentRoute.isHomeRoute()
    val shouldFloatNavigation = isPlatformNavigationBarFloating &&
            (currentRoute == AppRoute.Settings || currentRoute == AppRoute.Gallery)
    val isDrawerObscuringContent by remember(drawerState, shouldFloatNavigation, drawerWidth) {
        derivedStateOf {
            !drawerState.isClosed || drawerState.targetValue != DrawerValue.Closed ||
                    (shouldFloatNavigation && drawerWidth > 0 &&
                            drawerState.currentOffset > -drawerWidth.toFloat() + 0.5f)
        }
    }

    BoxWithConstraints(modifier) {
        val scope = rememberCoroutineScope()
        PlatformRotationEffect {
            scope.launch { drawerState.snapTo(DrawerValue.Closed) }
        }
        val shouldUseRail =
            !isPlatformToolbarNative && maxWidth >= 600.dp && shouldShowHomeNavigation
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = LocalNavigationInputEnabled.current && (drawerState.isOpen || shouldShowHomeNavigation),
            drawerContent = {
                DrawerSheetContent(
                    modifier = Modifier.onSizeChanged { drawerWidth = it.width },
                    items = appDrawerItems(
                        currentRoute = currentRoute,
                        settings = settings,
                        buildInfoProvider = buildInfoProvider,
                        onAction = processAction,
                    ),
                    header = {
                        DrawerHeader(buildInfoProvider = buildInfoProvider)
                    },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(Modifier.fillMaxSize()) {
                if (shouldUseRail) {
                    HomeNavigationRail(appBottomNavigationItems(currentRoute, processAction))
                }
                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = if (isPlatformToolbarNative && shouldShowHomeNavigation &&
                        !shouldFloatNavigation
                    ) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (shouldShowHomeNavigation && !shouldUseRail) {
                            HomeNavigationBar(
                                items = appBottomNavigationItems(
                                    currentRoute = currentRoute,
                                    onAction = processAction,
                                ),
                                hasBackground = !shouldFloatNavigation,
                                isObscured = isDrawerObscuringContent,
                            )
                        }
                    },
                    topBar = {
                        if (!isPlatformToolbarNative) Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsTopHeight(persistentTopAppBarWindowInsets())
                                .background(TopAppBarDefaults.topAppBarColors().containerColor)
                        )
                    },
                ) { paddingValues ->
                    val density = LocalDensity.current
                    val direction = LocalLayoutDirection.current
                    val systemBottomInset = with(density) {
                        persistentBottomBarWindowInsets().getBottom(this).toDp()
                    }
                    val floatingInset =
                        if (shouldFloatNavigation) maxOf(
                            paddingValues.calculateBottomPadding(),
                            systemBottomInset
                        ) else 0.dp
                    val bodyBottomPadding = if (shouldShowHomeNavigation && !shouldUseRail &&
                        !shouldFloatNavigation
                    ) maxOf(paddingValues.calculateBottomPadding(), systemBottomInset)
                    else if (shouldFloatNavigation) 0.dp else paddingValues.calculateBottomPadding()
                    val bodyPadding = PaddingValues(
                        start = paddingValues.calculateStartPadding(direction),
                        end = paddingValues.calculateEndPadding(direction),
                        top = paddingValues.calculateTopPadding(),
                        bottom = bodyBottomPadding,
                    )
                    CompositionLocalProvider(
                        LocalFloatingNavigationBarInset provides floatingInset,
                        LocalToolbarObscured provides isDrawerObscuringContent,
                    ) {
                        connectivityContent { connectivity ->
                            HomeContentLayout(
                                hasNativeToolbar = isPlatformToolbarNative && shouldShowHomeNavigation,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bodyPadding)
                                    .consumeWindowInsets(bodyPadding),
                                status = {
                                    if (shouldShowHomeNavigation) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isPlatformToolbarNative) Color.Transparent
                                                    else MaterialTheme.colorScheme.surface
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            ConnectivityWidget(connectivity)
                                        }
                                    }
                                },
                            ) {
                                content(Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}
