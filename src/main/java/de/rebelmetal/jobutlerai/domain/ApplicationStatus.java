package de.rebelmetal.jobutlerai.domain;

/**
 * Represents the lifecycle state of a scraped job posting
 * within the JoButler-AI application.
 *
 * IMPORTANT: This enum is mapped with EnumType.STRING in JobPost.
 * EnumType.ORDINAL is forbidden — inserting a new constant at any position
 * other than the end would silently corrupt all existing database rows.
 */
public enum ApplicationStatus {

    /** Freshly scraped, not yet reviewed by the user or AI. */
    NEW,

    /** AI pipeline has processed the posting (rating + summary generated). */
    AI_REVIEWED,

    /** User has manually reviewed the AI output. */
    USER_REVIEWED,

    /** User submitted an application for this posting. */
    APPLIED,

    /** Posting is no longer relevant or was rejected. */
    ARCHIVED
}
