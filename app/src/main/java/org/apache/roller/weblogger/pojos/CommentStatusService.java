/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.pojos;

import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;

/**
 * Service for handling comment status operations.
 * Extracted from WeblogEntryComment to address Cyclic-Dependent Modularization.
 * 
 * <p>This service provides:</p>
 * <ul>
 *   <li>Status checking utilities</li>
 *   <li>Status transition validation</li>
 *   <li>Bulk status operations</li>
 * </ul>
 */
public class CommentStatusService {

    /**
     * Check if a comment is marked as spam.
     * 
     * @param comment The comment to check
     * @return true if spam
     */
    public boolean isSpam(WeblogEntryComment comment) {
        return ApprovalStatus.SPAM.equals(comment.getStatus());
    }

    /**
     * Check if a comment is pending approval.
     * 
     * @param comment The comment to check
     * @return true if pending
     */
    public boolean isPending(WeblogEntryComment comment) {
        return ApprovalStatus.PENDING.equals(comment.getStatus());
    }

    /**
     * Check if a comment is approved.
     * 
     * @param comment The comment to check
     * @return true if approved
     */
    public boolean isApproved(WeblogEntryComment comment) {
        return ApprovalStatus.APPROVED.equals(comment.getStatus());
    }

    /**
     * Check if a comment is disapproved.
     * 
     * @param comment The comment to check
     * @return true if disapproved
     */
    public boolean isDisapproved(WeblogEntryComment comment) {
        return ApprovalStatus.DISAPPROVED.equals(comment.getStatus());
    }

    /**
     * Check if a comment is visible (approved and not spam).
     * 
     * @param comment The comment to check
     * @return true if visible
     */
    public boolean isVisible(WeblogEntryComment comment) {
        return isApproved(comment) && !isSpam(comment);
    }

    /**
     * Mark a comment as spam.
     * 
     * @param comment The comment to mark
     */
    public void markAsSpam(WeblogEntryComment comment) {
        comment.setStatus(ApprovalStatus.SPAM);
    }

    /**
     * Approve a comment.
     * 
     * @param comment The comment to approve
     */
    public void approve(WeblogEntryComment comment) {
        comment.setStatus(ApprovalStatus.APPROVED);
    }

    /**
     * Disapprove a comment.
     * 
     * @param comment The comment to disapprove
     */
    public void disapprove(WeblogEntryComment comment) {
        comment.setStatus(ApprovalStatus.DISAPPROVED);
    }

    /**
     * Set comment to pending status.
     * 
     * @param comment The comment to set pending
     */
    public void setPending(WeblogEntryComment comment) {
        comment.setStatus(ApprovalStatus.PENDING);
    }

    /**
     * Check if status transition is valid.
     * 
     * @param from Current status
     * @param to Target status
     * @return true if transition is allowed
     */
    public boolean isValidTransition(ApprovalStatus from, ApprovalStatus to) {
        if (from == null || to == null) {
            return false;
        }
        // All transitions are currently allowed
        // This method provides a hook for future business rules
        return true;
    }

    /**
     * Get display name for a status.
     * 
     * @param status The status
     * @return Human-readable status name
     */
    public String getStatusDisplayName(ApprovalStatus status) {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case APPROVED:
                return "Approved";
            case DISAPPROVED:
                return "Disapproved";
            case SPAM:
                return "Spam";
            case PENDING:
                return "Pending";
            default:
                return status.name();
        }
    }
}