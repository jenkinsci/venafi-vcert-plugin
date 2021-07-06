package io.jenkins.plugins.venafivcert;

public enum ConnectorType {
    TLS_PROTECT("TLS Protect", com.venafi.vcert.sdk.endpoint.ConnectorType.TPP_TOKEN),
    DEVOPS_ACCELERATE("Venafi as a Service", com.venafi.vcert.sdk.endpoint.ConnectorType.CLOUD);

    private final String displayName;
    private final com.venafi.vcert.sdk.endpoint.ConnectorType vcertConnectorType;

    ConnectorType(String displayName, com.venafi.vcert.sdk.endpoint.ConnectorType vcertConnectorType) {
        this.displayName = displayName;
        this.vcertConnectorType = vcertConnectorType;
    }

    com.venafi.vcert.sdk.endpoint.ConnectorType getVCertConnectorType() {
        return vcertConnectorType;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
