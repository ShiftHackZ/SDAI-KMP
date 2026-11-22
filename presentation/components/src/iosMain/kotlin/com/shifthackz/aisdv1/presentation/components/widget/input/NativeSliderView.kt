@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchCancel
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlEventTouchUpOutside
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UISlider
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import kotlin.math.roundToInt

/**
 * Hosts the system slider within a 48-point touch area and applies shared discrete stops.
 * Compose releases both callbacks when the view leaves composition.
 *
 * @property onValueChange Receives bounded user values from touch and native accessibility.
 * @property onFinished Receives gesture completion or cancellation.
 * @author Dmitriy Moroz
 */
internal class NativeSliderView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val slider = UISlider()
    private var valueRange = 0f..1f
    private var steps = 0
    var onValueChange: ((Float) -> Unit)? = null
    var onFinished: (() -> Unit)? = null

    init {
        addSubview(slider)
        slider.addTarget(this, NSSelectorFromString("valueChanged:"), UIControlEventValueChanged)
        slider.addTarget(
            this, NSSelectorFromString("gestureFinished:"),
            UIControlEventTouchUpInside or UIControlEventTouchUpOutside or UIControlEventTouchCancel,
        )
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        slider.setFrame(CGRectInset(bounds, 4.0, 0.0))
    }

    /**
     * Synchronizes the native control without generating value-change events.
     *
     * @param value Current shared parameter value.
     * @param valueRange Inclusive bounds.
     * @param steps Intermediate stop count, zero for continuous movement.
     * @param isEnabled Whether input is available.
     * @param isDark App appearance independent of the system theme.
     * @param isRtl Whether minimum values appear on the right.
     * @param accent App accent for the active track.
     * @param backingColor Parent color filling the Compose interop cutout.
     * @param label Localized parameter name for VoiceOver.
     * @param onChange Current shared-state event callback.
     * @param onFinished Current gesture completion callback.
     * @author Dmitriy Moroz
     */
    fun update(
        value: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int,
        isEnabled: Boolean,
        isDark: Boolean,
        isRtl: Boolean,
        accent: UIColor,
        backingColor: UIColor,
        label: String,
        onChange: (Float) -> Unit,
        onFinished: (() -> Unit)?,
    ) {
        require(steps >= 0)
        this.valueRange = valueRange.start..valueRange.endInclusive
        this.steps = steps
        this.onValueChange = onChange
        this.onFinished = onFinished
        backgroundColor = backingColor
        overrideUserInterfaceStyle =
            if (isDark) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        slider.semanticContentAttribute =
            if (isRtl) UISemanticContentAttributeForceRightToLeft else UISemanticContentAttributeForceLeftToRight
        slider.enabled = isEnabled
        slider.minimumValue = valueRange.start
        slider.maximumValue = valueRange.endInclusive
        slider.tintColor = accent
        slider.minimumTrackTintColor = accent
        slider.accessibilityLabel = label
        slider.setValue(value.coerceIn(valueRange), animated = false)
    }

    /**
     * Aligns native input to the same endpoint-inclusive stops as Material.
     *
     * @param sender Slider after touch or accessibility adjustment.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun valueChanged(sender: UISlider) {
        val span = valueRange.endInclusive - valueRange.start
        val bounded = sender.value.coerceIn(valueRange)
        val value = if (steps > 0 && span > 0f) {
            val intervals = steps + 1
            valueRange.start + ((bounded - valueRange.start) / span * intervals).roundToInt() * span / intervals
        } else bounded
        sender.setValue(value, animated = false)
        onValueChange?.invoke(value)
    }

    /**
     * Ends the current edit without inventing an additional parameter change.
     *
     * @param sender Slider whose touch sequence ended.
     * @author Dmitriy Moroz
     */
    @ObjCAction
    fun gestureFinished(sender: UISlider) {
        if (!sender.tracking) onFinished?.invoke()
    }
}
