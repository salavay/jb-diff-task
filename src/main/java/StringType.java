/**
 * Enum which is used to describe transitions of LCM algorithm
 */
public enum StringType {
    /**
     * Line that has been changed
     */
    CHANGEDOLD("changed-text-old"),
    /**
     * New version of changed line
     */
    CHANGEDNEW("changed-text-new"),
    /**
     * Line was deleted
     */
    DELETED("deleted-text"),
    /**
     * Means that string was added in new file
     */
    ADDED("added-text"),
    /**
     * Means that string hasn't been changed
     */
    OLD("old-text");

    private final String cssClassName;

    StringType(final String cssClassName) {
        this.cssClassName = cssClassName;
    }

    @Override
    public String toString() {
        return cssClassName;
    }
}
