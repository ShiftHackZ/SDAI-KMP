package com.shifthackz.aisdv1.domain.entity

/**
 * Requested MNN execution backend; OpenCL availability never implies all operators run on GPU.
 *
 * @param key Stable configuration value.
 *
 * @author Dmitriy Moroz
 */
enum class MnnBackend(val key: String) {
    CPU("cpu"), OPENCL("opencl");

    companion object {
        /**
         * Restores a saved backend; absent legacy configuration selects CPU.
         *
         * @param value Persisted backend key.
         *
         * @author Dmitriy Moroz
         */
        fun parse(value: String?) = entries.firstOrNull { it.key == value } ?: CPU
    }
}
