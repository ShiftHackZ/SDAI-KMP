package com.shifthackz.aisdv1.presentation.benchmark.preview

import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkState

/**
 * Names a fixed benchmark snapshot and the section initially visible in its scrolling list.
 *
 * @param name Label identifying the score, hardware or recommendation state.
 * @param state Fixed UI data; no hardware probe or benchmark operation is invoked.
 * @param sectionIndex Initial lazy-list item: score0, hardware1, capabilities2 or recommendations3.
 * @author Dmitriy Moroz
 */
internal data class BenchmarkPreviewCase(
    val name: String,
    val state: BenchmarkState,
    val sectionIndex: Int = 0,
)
