package org.apache.roller.weblogger.business;

import java.util.List;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Star/Favourite functionality.
 */
public class StarTest {

    private User testUser = null;
    private Weblog testWeblog1 = null;
    private Weblog testWeblog2 = null;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        String timestamp = String.valueOf(System.currentTimeMillis());
        testUser = TestUtils.setupUser("user" + timestamp);
        testWeblog1 = TestUtils.setupWeblog("weblog1" + timestamp, testUser);
        testWeblog2 = TestUtils.setupWeblog("weblog2" + timestamp, testUser);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            if (testWeblog1 != null) TestUtils.teardownWeblog(testWeblog1.getId());
        } catch (Exception e) {}
        try {
            if (testWeblog2 != null) TestUtils.teardownWeblog(testWeblog2.getId());
        } catch (Exception e) {}
        try {
            if (testUser != null) TestUtils.teardownUser(testUser.getUserName());
        } catch (Exception e) {}
        TestUtils.endSession(true);
    }

    @Test
    public void testStarWeblog() throws Exception {
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());

        // Initially not starred
        assertFalse(umgr.isStarredWeblog(testUser, testWeblog1));
        assertEquals(0, umgr.getStarredWeblogs(testUser).size());

        // Star it
        umgr.toggleStarWeblog(testUser, testWeblog1);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());

        assertTrue(umgr.isStarredWeblog(testUser, testWeblog1));
        assertEquals(1, umgr.getStarredWeblogs(testUser).size());

        // Unstar it
        umgr.toggleStarWeblog(testUser, testWeblog1);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());

        assertFalse(umgr.isStarredWeblog(testUser, testWeblog1));
        assertEquals(0, umgr.getStarredWeblogs(testUser).size());
    }

    @Test
    public void testStarWeblogSorting() throws Exception {
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());
        testWeblog2 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog2.getId());

        // Star both
        umgr.toggleStarWeblog(testUser, testWeblog1);
        umgr.toggleStarWeblog(testUser, testWeblog2);
        TestUtils.endSession(true);

        // Add entries with different times
        WeblogEntry entry1 = TestUtils.setupWeblogEntry("entry1", testWeblog1, testUser);
        entry1.setPubTime(new java.sql.Timestamp(System.currentTimeMillis() - 100000));
        emgr.saveWeblogEntry(entry1);
        
        WeblogEntry entry2 = TestUtils.setupWeblogEntry("entry2", testWeblog2, testUser);
        entry2.setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        emgr.saveWeblogEntry(entry2);
        
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());

        List<Weblog> starred = umgr.getStarredWeblogs(testUser);
        assertEquals(2, starred.size());
        
        // testWeblog2 should be first because it has a more recent entry
        assertEquals(testWeblog2.getId(), starred.get(0).getId());
        assertEquals(testWeblog1.getId(), starred.get(1).getId());
        
        TestUtils.teardownWeblogEntry(entry1.getId());
        TestUtils.teardownWeblogEntry(entry2.getId());
    }

    @Test
    public void testStarEntry() throws Exception {
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());

        WeblogEntry entry = TestUtils.setupWeblogEntry("testEntry", testWeblog1, testUser);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());

        // Initially not starred
        assertFalse(umgr.isStarredEntry(testUser, entry));
        assertEquals(0, umgr.getStarredEntries(testUser, 0, 10).size());

        // Star it
        umgr.toggleStarEntry(testUser, entry);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());

        assertTrue(umgr.isStarredEntry(testUser, entry));
        assertEquals(1, umgr.getStarredEntries(testUser, 0, 10).size());
        assertEquals(1, umgr.getStarredEntriesCount(testUser));

        // Unstar it
        umgr.toggleStarEntry(testUser, entry);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());

        assertFalse(umgr.isStarredEntry(testUser, entry));
        assertEquals(0, umgr.getStarredEntriesCount(testUser));
        
        TestUtils.teardownWeblogEntry(entry.getId());
    }

    @Test
    public void testLikeCount() throws Exception {
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Set up a second user to like the same entry
        User testUser2 = TestUtils.setupUser("user2" + timestamp);
        TestUtils.endSession(true);

        testUser = umgr.getUserByUserName(testUser.getUserName());
        testUser2 = umgr.getUserByUserName(testUser2.getUserName());
        testWeblog1 = WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(testWeblog1.getId());

        WeblogEntry entry = TestUtils.setupWeblogEntry("likeEntry", testWeblog1, testUser);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testUser2 = umgr.getUserByUserName(testUser2.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());

        // Initially no likes
        assertEquals(0, umgr.getLikeCount(entry));

        // User 1 likes the entry -> count = 1
        umgr.toggleStarEntry(testUser, entry);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testUser2 = umgr.getUserByUserName(testUser2.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());
        assertEquals(1, umgr.getLikeCount(entry));
        assertTrue(umgr.isStarredEntry(testUser, entry));
        assertFalse(umgr.isStarredEntry(testUser2, entry));

        // User 2 also likes the entry -> count = 2
        umgr.toggleStarEntry(testUser2, entry);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        testUser2 = umgr.getUserByUserName(testUser2.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());
        assertEquals(2, umgr.getLikeCount(entry));
        assertTrue(umgr.isStarredEntry(testUser, entry));
        assertTrue(umgr.isStarredEntry(testUser2, entry));

        // User 1 unlikes the entry -> count back to 1
        umgr.toggleStarEntry(testUser, entry);
        TestUtils.endSession(true);
        testUser = umgr.getUserByUserName(testUser.getUserName());
        entry = emgr.getWeblogEntry(entry.getId());
        assertEquals(1, umgr.getLikeCount(entry));
        assertFalse(umgr.isStarredEntry(testUser, entry));

        // Cleanup
        TestUtils.teardownWeblogEntry(entry.getId());
        TestUtils.teardownUser(testUser2.getUserName());
    }
}
