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
void mnn_attempt_interrupt(MnnAttempt *attempt);
void mnn_attempt_destroy(MnnAttempt *attempt);
int mnn_metal_available(void);
#ifdef __cplusplus
}
#endif
#endif
