package io.jenkins.plugins.venafivcert;

import com.venafi.vcert.sdk.VCertException;
import com.venafi.vcert.sdk.certificate.CertificateRequest;
import com.venafi.vcert.sdk.certificate.PEMCollection;
import com.venafi.vcert.sdk.connectors.ZoneConfiguration;

public interface VCertClient {
    ZoneConfiguration readZoneConfiguration(String zone) throws VCertException;
    CertificateRequest generateRequest(ZoneConfiguration config, CertificateRequest request) throws VCertException;
    String requestCertificate(CertificateRequest request, ZoneConfiguration zoneConfiguration) throws VCertException, UnsupportedOperationException;
    PEMCollection retrieveCertificate(CertificateRequest request) throws VCertException;
}
