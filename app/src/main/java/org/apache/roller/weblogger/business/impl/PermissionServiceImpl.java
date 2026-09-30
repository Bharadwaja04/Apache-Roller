package org.apache.roller.weblogger.business.impl;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.api.PermissionService;
import org.apache.roller.weblogger.api.ids.UserId;
import org.apache.roller.weblogger.api.ids.WeblogId;

/**
 * Minimal adapter/stub implementation of PermissionService.
 *
 * This class lives in the implementation layer and is the place to map
 * UserId/WeblogId -> domain POJOs and invoke existing manager/DAO logic.
 */
public class PermissionServiceImpl implements PermissionService {

    @Override
    public void grantWeblogPermission(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException {
        throw new WebloggerException("Not implemented: map ids to domain and delegate");
    }

    @Override
    public void grantWeblogPermissionPending(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException {
        throw new WebloggerException("Not implemented: map ids to domain and delegate");
    }

    @Override
    public void confirmWeblogPermission(WeblogId weblogId, UserId userId)
            throws WebloggerException {
        throw new WebloggerException("Not implemented: map ids to domain and delegate");
    }

    @Override
    public void declineWeblogPermission(WeblogId weblogId, UserId userId)
            throws WebloggerException {
        throw new WebloggerException("Not implemented: map ids to domain and delegate");
    }

    @Override
    public void revokeWeblogPermission(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException {
        throw new WebloggerException("Not implemented: map ids to domain and delegate");
    }
}
