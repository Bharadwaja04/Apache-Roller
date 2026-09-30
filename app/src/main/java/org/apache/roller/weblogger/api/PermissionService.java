package org.apache.roller.weblogger.api;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.api.ids.UserId;
import org.apache.roller.weblogger.api.ids.WeblogId;

/**
 * Narrow permission service interface using lightweight ids.
 */
public interface PermissionService {

    void grantWeblogPermission(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException;

    void grantWeblogPermissionPending(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException;

    void confirmWeblogPermission(WeblogId weblogId, UserId userId)
            throws WebloggerException;

    void declineWeblogPermission(WeblogId weblogId, UserId userId)
            throws WebloggerException;

    void revokeWeblogPermission(WeblogId weblogId, UserId userId, List<String> actions)
            throws WebloggerException;
}
