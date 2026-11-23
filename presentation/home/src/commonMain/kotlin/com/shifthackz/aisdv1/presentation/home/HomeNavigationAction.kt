package com.shifthackz.aisdv1.presentation.home

/**
 * Destination selections emitted by the home tabs and drawer.
 * The application host translates these into its existing navigation operations.
 *
 * @author Dmitriy Moroz
 */
internal enum class HomeNavigationAction {
    TextToImage,
    ImageToImage,
    Gallery,
    Settings,
    WebUi,
    Configuration,
    Debug,
}
