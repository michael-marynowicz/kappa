package com.company.sprintreporter.application.dto.invitation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkInviteResultDto {

    /** Number of new invitations created and emails queued. */
    private int invited;

    /** Emails skipped because they already have a PENDING invitation. */
    private int alreadyPending;

    /** Emails skipped because they are already a member. */
    private int alreadyMember;

    /** Emails skipped because they were invalid (bad format, blank, etc.). */
    private int invalid;
}
