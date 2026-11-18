package com.shifthackz.aisdv1.presentation.donate

import androidx.compose.ui.Modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import com.shifthackz.aisdv1.presentation.components.platform.rememberExternalUrlLauncher
import org.koin.core.parameter.parametersOf

/**
 * Binds donation content to its remembered ViewModel and opens support links through the platform handler.
 *
 * @param modifier Layout modifiers for the donation screen.
 * @param router Optional back-navigation owner; defaults to the application graph binding.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DonateScreen(
    modifier: Modifier = Modifier,
    router: DonateRouter? = null,
) {
    val koin = remember { getKoin() }
    val urlLauncher = rememberExternalUrlLauncher()
    val donateRouter = remember(koin, router) {
        router ?: koin.get<DonateRouter>()
    }
    val viewModel = remember(koin, donateRouter) {
        koin.get<DonateViewModel> {
            parametersOf(donateRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                is DonateEffect.OpenUrl -> urlLauncher.openUrl(effect.url)
            }
        },
    ) { state, processIntent ->
        val strings = donateScreenStrings()
        DonateScreenContent(
            state = state,
            strings = strings,
            processIntent = processIntent,
            brandIcon = { DonateBrandIcon() },
            modifier = modifier,
        )
    }
}


@Composable
private fun donateScreenStrings() = DonateScreenStrings(
    title = Localization.string("settings_item_donate"),
    thanksTitle = Localization.string("donate_title_thanks"),
    bottomTitle = Localization.string("donate_bs_title"),
    bottomSubtitle = Localization.string("donate_bs_sub_title"),
    bottomEnding = Localization.string("donate_bs_ending"),
    backContentDescription = Localization.string("action_back"),
)

/**
 * Draws the donation-page platform branding.
 *
 * @param modifier Size and placement within the donation illustration.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun DonateBrandIcon(modifier: Modifier = Modifier)
