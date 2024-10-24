package org.openrewrite.jgit.ignore;

import org.junit.jupiter.api.Test;
import org.openrewrite.jgit.errors.InvalidPatternException;
import org.openrewrite.jgit.ignore.internal.PathMatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openrewrite.jgit.ignore.FastIgnoreRule.PATH_SEPARATOR;

class Issue2Test {

    @Test
    void fastIgnoreRuleForDirectory() {
        FastIgnoreRule ignoreRule = new FastIgnoreRule("/directory/");
        assertTrue(ignoreRule.isMatch("/directory/nested/", true, false));
        // BUG2: WHY IS /directory/nested/ not matched by a /directory/ gitignore rule?
    }

    @Test
    void thePathMatcherCreation() throws InvalidPatternException {
        // This rule creates a matcher internally using the PathMatcher class.
        // The bug: internally the matcher is created as a NameMatcher, whereas a complex path is given -> it should be a PathMatcher
        IMatcher matcher = PathMatcher.createPathMatcher("/directory", PATH_SEPARATOR, true);
        // If i override the matcher creation to delegate to constructor of PathMatcher (debug override indexes to go in if clause),
        // --> here by making directory 2 segmets, the middle slash will be found so delegating to PathMatcher constructor
        IMatcher secondMatcher = PathMatcher.createPathMatcher("/directory/nested", PATH_SEPARATOR, true);

        assertTrue(secondMatcher instanceof PathMatcher);
        assertTrue(matcher instanceof PathMatcher); //This one fails
    }

    @Test
    void pathMatcherHasIssueWhenSingleSegmentPath() throws InvalidPatternException {
        // If i override the matcher creation to delegate to constructor of PathMatcher (debug override/set indexes to go in if clause),
        // the construction is wrongly creating matchers (it should go in the if clause that checks for simple pattern)
        // --> due to isSimplePathWithSegments counting the slashes ignoring first/last ones -> 0 !> 0
        // As it do not go in there, i get java.lang.IllegalStateException: Pattern must have at least two segments: /directory/

        // If i have a patch starting with a slash AND ending with a slash, it should be a PatchMatcher, ignoring how many segments it has
        // If there are no wildcards in the pattern, it should be a PathMatchers simpleMatch.
        PathMatcher matcher = new PathMatcher("/directory", PATH_SEPARATOR, true);

        // see that the matchers property is not empty, whereas it should be so the matches call would delegate to the simpleMatch
    }

    @Test
    void indexesOfSlashMatter() {
        // If i override the matcher creation to delegate to constructor of PathMatcher (debug override indexes to go in if clause),
        // the construction is wrongly creating matchers (it should go in the if clause that checks for simple pattern)
        // As it doe not go in there, i get java.lang.IllegalStateException: Pattern must have at least two segments: /directory/
        // If i have a patch starting with a slash AND ending with a slash, it should be a PatchMatcher, ignoring how many segments it has
        // If there are no wildcards in the pattern, it should be a PathMatchers simpleMatch.
        FastIgnoreRule ignoreRule = new FastIgnoreRule("/directory/");
        assertTrue(ignoreRule.isMatch("/directory/nested/", true, false));
    }
}
