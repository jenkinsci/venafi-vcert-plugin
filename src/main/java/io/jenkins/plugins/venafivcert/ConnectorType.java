package io.jenkins.plugins.venafivcert;

public enum ConnectorType {
    TPP("TPP"),
    CLOUD("Cloud");

    private String displayName;

    ConnectorType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
