public enum StringType {
    CHANGEDOLD("changed-text-old"),
    CHANGEDNEW("changed-text-new"),
    DELETED("deleted-text"),
    ADDED("added-text"),
    OLD("old-text");

    private final String cssClassName;

    StringType(final String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    @Override
    public String toString() {
        return cssClassName;
    }
}
