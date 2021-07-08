package io.jenkins.plugins.venafivcert;

import com.venafi.vcert.sdk.Config;
import com.venafi.vcert.sdk.VCertException;
import com.venafi.vcert.sdk.certificate.CertificateRequest;
import com.venafi.vcert.sdk.certificate.PEMCollection;
import com.venafi.vcert.sdk.connectors.ZoneConfiguration;
import com.venafi.vcert.sdk.endpoint.Authentication;

import hudson.AbortException;

public class NormalVCertClient implements VCertClient {
    private com.venafi.vcert.sdk.VCertClient realClient;

    public NormalVCertClient(Config config, Authentication auth) throws AbortException {
        try {
            realClient = new com.venafi.vcert.sdk.VCertClient(config);
        } catch (VCertException e) {
            throw new AbortException("Error creating VCert client: " + e.getMessage());
        }

        try {
            realClient.authenticate(auth);
        } catch (VCertException e) {
            throw new AbortException("Error authenticating VCert: " + e.getMessage());
        }
    }

    public ZoneConfiguration readZoneConfiguration(String zone) throws VCertException {
        return realClient.readZoneConfiguration(zone);
    }

    public CertificateRequest generateRequest(ZoneConfiguration config, CertificateRequest request) throws VCertException {
        return realClient.generateRequest(config, request);
    }

    public String requestCertificate(CertificateRequest request, ZoneConfiguration zoneConfiguration) throws VCertException, UnsupportedOperationException {
        return realClient.requestCertificate(request, zoneConfiguration);
    }

    public PEMCollection retrieveCertificate(CertificateRequest request) throws VCertException {
        return realClient.retrieveCertificate(request);
    }
}
