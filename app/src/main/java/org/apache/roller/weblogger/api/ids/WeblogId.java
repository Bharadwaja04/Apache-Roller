package org.apache.roller.weblogger.api.ids;

import java.io.Serializable;

/**
 * Lightweight identifier for a Weblog.
 */
public final class WeblogId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    public WeblogId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
