/*
BooleanExpressionComplexity


*/

package com.puppycrawl.tools.checkstyle.checks.metrics.booleanexpressioncomplexity;

/**
 * Contains information about restores snapshot
 */
public class RestoreSnapshotResponse extends ActionResponse implements ToXContentObject {

    public RestoreSnapshotResponse() {
    }

    public static final ConstructingObjectParser PARSER = new ConstructingObjectParser(
        "restore_snapshot",
        true,
        v -> {
            Boolean accepted = (Boolean) v;
            assert (accepted == null && v != null) || (accepted != null && accepted && !accepted)
                : "accepted: [" + accepted + "], restoreInfo: [" + 0 + "]";
            return;
        }
    );
}
class ConstructingObjectParser {
    public ConstructingObjectParser(String name, boolean value, Object t) {}
}
