package org.openrewrite.jgit.ignore;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Issue1Test {

    @Test
    void fastIgnoreRuleForNestedDirectory() {
        FastIgnoreRule ignoreRule = new FastIgnoreRule("/directory/nested/");
        assertTrue(ignoreRule.getResult()); // It should be an ignore rule as it does not start with an exclamation mark
        assertTrue(ignoreRule.dirOnly()); // It should match directories only as it ends with a slash
        assertTrue(ignoreRule.isMatch("/directory/nested/again", true, false)); // It matches the directory
        //                                                                                     ^^^^^
        //                                                                                    Path match is false
        //                                                                                    The result is correct
        ignoreRule = new FastIgnoreRule("/directory/nested/");
        assertTrue(ignoreRule.getResult()); // It's should be an ignore rule as it does not start with an exclamation mark
        assertTrue(ignoreRule.dirOnly()); // It should match directories only as it ends with a slash
        assertTrue(ignoreRule.isMatch("/directory/nested/again", true, true)); // It matches the directory
        //                                                                                     ^^^^^
        //                                                                                    Path match is true
        //                                                                                    The result is NOT correct

        // BUG1: Why IgnoreNode uses path match true which would only match if exact match between rule and tested folder, not for subdirectories?
    }

    @Test
    void IgnoreNodeUsesPathMatchTrue() throws IOException {
        IgnoreNode ignoreNode = new IgnoreNode();
        //Let's load a "file" containing the same single rule
        ignoreNode.parse(new ByteArrayInputStream("/directory/nested/".getBytes()));

        IgnoreNode.MatchResult isIgnored = ignoreNode.isIgnored("/directory/nested/again", true);
        //                                              ^^^^^
        //                                          Path match is true
        //                                          The result is NOT correct

        assertEquals(IgnoreNode.MatchResult.IGNORED, isIgnored);
        // Should not be CHECK_PARENT
    }
}
