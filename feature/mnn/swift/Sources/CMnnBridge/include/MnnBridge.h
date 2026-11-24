#ifndef SDAI_MNN_BRIDGE_H
#define SDAI_MNN_BRIDGE_H
#include <stdint.h>
#ifdef __cplusplus
extern "C" {
#endif
typedef struct MnnAttempt MnnAttempt;
typedef void (*MnnProgress)(void *context, int percent);
/* Each handle has one executing owner. Interrupt may run concurrently; destroy
 * must run only after generate returns. Generate returns 0 on success, 1 when
 * cancelled, and 2 on failure. Progress must not throw across this C ABI. */
MnnAttempt *mnn_attempt_create(void);
int mnn_attempt_generate(MnnAttempt *attempt, const char *model, const char *prompt,
    const char *output, const char *workspace, int steps, int32_t seed,
    int use_metal, MnnProgress progress, void *context);
/* Runs RGB8 super-resolution into caller-owned width*height*16*3 output bytes.
 * Input dimensions must be 1..1024. The same one-owner/cancellation rules apply. */
int mnn_attempt_upscale(MnnAttempt *attempt, const char *model,
    const uint8_t *input, int width, int height, uint8_t *output,
    MnnProgress progress, void *context);
void mnn_attempt_interrupt(MnnAttempt *attempt);
void mnn_attempt_destroy(MnnAttempt *attempt);
int mnn_metal_available(void);
#ifdef __cplusplus
}
#endif
#endif
