package com.shifthackz.aisdv1.domain.usecase.report

import com.shifthackz.aisdv1.domain.entity.ReportReason
import com.shifthackz.aisdv1.domain.repository.ReportRepository

/**
 * Submits a user report with its reason and attached image.
 *
 * @param repository Delivers the report to the configured reporting destination.
 *
 * @author Dmitriy Moroz
 */
internal class SendReportUseCaseImpl(
    private val repository: ReportRepository,
) : SendReportUseCase {

    /**
     * Sends the supplied report and propagates delivery failures.
     *
     * @param text User-provided description of the issue.
     * @param reason Report category selected by the user.
     * @param image Image attachment passed to the reporting repository.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(text: String, reason: ReportReason, image: String) =
        repository.send(text, reason, image)

}
