package com.shifthackz.aisdv1.core.common.file

/**
 * Suffix for the sibling `.<root-name>.inference.lock` owned by a native model reader.
 * Host payload deletion and replacement must reserve the same lock before mutation;
 * keeping it outside the payload directory preserves its identity across activation.
 *
 * @author Dmitriy Moroz
 */
const val MNN_INFERENCE_LOCK_SUFFIX = ".inference.lock"
