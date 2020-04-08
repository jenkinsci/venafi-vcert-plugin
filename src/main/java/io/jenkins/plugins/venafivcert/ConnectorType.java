package io.jenkins.plugins.venafivcert;

public enum ConnectorType {
    TPP("TPP"),
    CLOUD("Cloud");

    private String displayName;

    static ConnectorType valueOfDisplayName(String displayName) {
        for (ConnectorType type: ConnectorType.class.getEnumConstants()) {
            if (type.toString().equals(displayName)) {
                return type;
            }
        }
        return null;
    }

    ConnectorType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
