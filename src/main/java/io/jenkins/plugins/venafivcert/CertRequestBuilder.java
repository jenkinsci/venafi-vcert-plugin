package io.jenkins.plugins.venafivcert;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.venafi.vcert.sdk.Config;
import com.venafi.vcert.sdk.VCertException;
import com.venafi.vcert.sdk.Config.ConfigBuilder;
import com.venafi.vcert.sdk.certificate.CertificateRequest;
import com.venafi.vcert.sdk.certificate.KeyType;
import com.venafi.vcert.sdk.certificate.PEMCollection;
import com.venafi.vcert.sdk.connectors.ZoneConfiguration;
import com.venafi.vcert.sdk.endpoint.Authentication;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;

public class CertRequestBuilder extends Builder implements SimpleBuildStep {
    public static final String OAUTH_SCOPE = "certificate:manage";

    private final String connectorName;
    private final String zoneConfigName;
    private final String commonName;
    private final String privKeyOutput;
    private final String certOutput;
    private final String certChainOutput;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private KeyType keyType;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private List<DnsName> dnsNames;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private List<IpAddress> ipAddresses;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private List<EmailAddress> emailAddresses;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String organization;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String organizationalUnit;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String locality;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String province;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String country;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private int expirationWindow;

    @DataBoundConstructor
    public CertRequestBuilder(String connectorName, String zoneConfigName, String commonName,
        String privKeyOutput, String certOutput, String certChainOutput)
    {
        this.connectorName = connectorName;
        this.zoneConfigName = zoneConfigName;
        this.commonName = commonName;
        this.privKeyOutput = privKeyOutput;
        this.certOutput = certOutput;
        this.certChainOutput = certChainOutput;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public String getZoneConfigName() {
        return zoneConfigName;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    @DataBoundSetter
    public void setKeyType(KeyType value) {
        this.keyType = value;
    }

    public List<DnsName> getDnsNames() {
        if (dnsNames == null) {
            return Collections.emptyList();
        } else {
            return dnsNames;
        }
    }

    @DataBoundSetter
    public void setDnsNames(List<DnsName> value) {
        this.dnsNames = value;
    }

    public List<IpAddress> getIpAddresses() {
        if (ipAddresses == null) {
            return Collections.emptyList();
        } else {
            return ipAddresses;
        }
    }

    @DataBoundSetter
    public void setIpAddresses(List<IpAddress> value) {
        this.ipAddresses = value;
    }

    public List<EmailAddress> getEmailAddresses() {
        if (emailAddresses == null) {
            return Collections.emptyList();
        } else {
            return emailAddresses;
        }
    }

    @DataBoundSetter
    public void setEmailAddresses(List<EmailAddress> value) {
        this.emailAddresses = value;
    }

    public String getCommonName() {
        return commonName;
    }

    public int getExpirationWindow() {
        return expirationWindow;
    }

    @DataBoundSetter
    public void setExpirationWindow(int value) {
        expirationWindow = value;
    }

    public String getOrganization() {
        return organization;
    }

    @DataBoundSetter
    public void setOrganization(String value) {
        this.organization = value;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    @DataBoundSetter
    public void setOrganizationalUnit(String value) {
        this.organizationalUnit = value;
    }

    public String getLocality() {
        return locality;
    }

    @DataBoundSetter
    public void setLocality(String value) {
        this.locality = value;
    }

    public String getProvince() {
        return province;
    }

    @DataBoundSetter
    public void setProvince(String value) {
        this.province = value;
    }

    public String getCountry() {
        return country;
    }

    @DataBoundSetter
    public void setCountry(String value) {
        this.country = value;
    }

    public String getPrivKeyOutput() {
        return privKeyOutput;
    }

    public String getCertOutput() {
        return certOutput;
    }

    public String getCertChainOutput() {
        return certChainOutput;
    }

    @Override
    public void perform(@Nonnull Run<?,?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
        @Nonnull TaskListener listener) throws InterruptedException, IOException
    {
        Logger logger = new Logger(listener.getLogger(), Messages.CertRequestBuilder_functionName());
        ZonedDateTime expirationTime;

        try {
            expirationTime = getPrevCertExpirationTime(workspace);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new AbortException("Error reading existing certificate's value: " + e.getMessage());
        }

        if (!withinExpirationWindow(logger, expirationTime)) {
            logger.log("Previous certificate's expiry time (%s) is not within the expiration window of %d hours." +
                " Not requesting a certificate.", expirationTime, getExpirationWindow());
            return;
        }

        ConnectorConfig connectorConfig = getConnectorConfig();
        VCertClient client = createClient(run, connectorConfig);
        ZoneConfiguration zoneConfig = readZoneConfig(client);

        CertificateRequest certReq = new CertificateRequest();
        certReq
            .keyType(getKeyType())
            .dnsNames(getDnsNamesAsStrings())
            .ipAddresses(getIpAddressesAsInetAddresses())
            .emailAddresses(getEmailAddressesAsStrings());
        certReq.subject(
            new CertificateRequest.PKIXName()
                .commonName(getCommonName())
                .organization(Arrays.asList(getOrganization()))
                .organizationalUnit(Arrays.asList(getOrganizationalUnit()))
                .country(Arrays.asList(getCountry()))
                .locality(Arrays.asList(getLocality()))
                .province(Arrays.asList(getProvince())));

        certReq = requestCertificate(connectorConfig, client, zoneConfig, certReq);
        PEMCollection pemCollection = retrieveCertificate(connectorConfig, client, certReq);
        writeOutputFiles(workspace, pemCollection);
    }

    private boolean withinExpirationWindow(Logger logger, ZonedDateTime expirationTime) throws AbortException {
        if (expirationWindow == 0) {
            return true;
        } else if (expirationWindow < 0) {
            throw new AbortException("expirationWindow may not be a negative number");
        } else if (expirationTime == null) {
            logger.log("An expirationWindow is configured, but the previous certificate (%s) does not exist." +
                " Will proceed with requesting a new certificate.", getCertOutput());
            return true;
        } else {
            boolean result = ZonedDateTime.now().isAfter(expirationTime.minusHours(expirationWindow));
            if (result) {
                logger.log("Previous certificate's expiry time (%s) is within the expiration window of %d hours." +
                    " Will proceed with requesting a certificate.", expirationTime, getExpirationWindow());
            }
            return result;
        }
    }

    private ZonedDateTime getPrevCertExpirationTime(FilePath workspace)
        throws IOException, InterruptedException, CertificateException
    {
        FilePath certPath = workspace.child(getCertOutput());
        if (!certPath.exists()) {
            return null;
        }

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        try (InputStream input = certPath.read()) {
            X509Certificate cert = (X509Certificate) fact.generateCertificate(input);
            return ZonedDateTime.ofInstant(cert.getNotAfter().toInstant(), ZoneId.systemDefault());
        }
    }

    private List<String> getDnsNamesAsStrings() {
        return getDnsNames().stream()
            .map(el -> el.getHostName())
            .collect(Collectors.toList());
    }

    private List<String> getEmailAddressesAsStrings() {
        return getEmailAddresses().stream()
            .map(el -> el.getAddress())
            .collect(Collectors.toList());
    }

    private Collection<InetAddress> getIpAddressesAsInetAddresses()
        throws AbortException
    {
        try {
            Collection<InetAddress> result = new ArrayList<InetAddress>();
            for (IpAddress ipAddr: getIpAddresses()) {
                result.add(InetAddress.getByName(ipAddr.getAddress()));
            }
            return result;
        } catch (UnknownHostException e) {
            throw new AbortException("Error resolving one of the provided IP addresses: "
                + e.getMessage());
        }
    }

    private Config createSdkConfig(ConnectorConfig connectorConfig) {
        ConfigBuilder sdkConfig = Config.builder();
        sdkConfig.connectorType(connectorConfig.getType().getVCertConnectorType());
        sdkConfig.appInfo(Messages.apiVendorAndProductName());
        if (connectorConfig.getType() == ConnectorType.TLS_PROTECT) {
            sdkConfig.baseUrl(connectorConfig.getTppBaseUrl());
        }
        return sdkConfig.build();
    }

    private Authentication createSdkAuthObject(Run<?, ?> run, ConnectorConfig connectorConfig) {
        if (connectorConfig.getType() == ConnectorType.TLS_PROTECT) {
            StandardUsernamePasswordCredentials credentials = Utils.findCredentials(
                StandardUsernamePasswordCredentials.class,
                connectorConfig.getTppCredentialsId(),
                null);
            CredentialsProvider.track(run, credentials);
            return Authentication.builder()
                .user(credentials.getUsername())
                .password(Secret.toString(credentials.getPassword()))
                .scope(OAUTH_SCOPE)
                .clientId(Messages.authClientID())
                .build();
        } else {
            assert connectorConfig.getType() == ConnectorType.DEVOPS_ACCELERATE;
            StringCredentials credentials = Utils.findCredentials(
                StringCredentials.class,
                connectorConfig.getCloudCredentialsId(),
                null);
            CredentialsProvider.track(run, credentials);
            return Authentication.builder()
                .apiKey(Secret.toString(credentials.getSecret()))
                .scope(OAUTH_SCOPE)
                .clientId(Messages.authClientID())
                .build();
        }
    }

    private ConnectorConfig getConnectorConfig() throws AbortException {
        ConnectorConfig result = PluginConfig.get()
            .getConnectorConfigByName(getConnectorName());
        if (result == null) {
            throw new AbortException("No Venafi VCert connector configuration with name '"
                + getConnectorName() + "' found");
        } else {
            return result;
        }
    }

    private VCertClient createClient(Run<?, ?> run, ConnectorConfig connectorConfig) throws AbortException {
        Config sdkConfig = createSdkConfig(connectorConfig);
        Authentication sdkAuth = createSdkAuthObject(run, connectorConfig);
        if (connectorConfig.getType() == ConnectorType.TLS_PROTECT) {
            return new TokenVCertClient(sdkConfig, sdkAuth);
        } else {
            assert connectorConfig.getType() == ConnectorType.DEVOPS_ACCELERATE;
            return new NormalVCertClient(sdkConfig, sdkAuth);
        }
    }

    private ZoneConfiguration readZoneConfig(VCertClient client) throws AbortException {
        try {
            return client.readZoneConfiguration(getZoneConfigName());
        } catch (VCertException e) {
            throw new AbortException("Error reading VCert zone configuration: "
                + e);
        }
    }

    private CertificateRequest requestCertificate(ConnectorConfig connectorConfig, VCertClient client,
        ZoneConfiguration zoneConfig, CertificateRequest certReq) throws AbortException
    {
        try {
            certReq = client.generateRequest(zoneConfig, certReq);
        } catch (VCertException e) {
            throw new AbortException("Error generating certificate request: "
                + e.getMessage());
        }

        try {
            client.requestCertificate(certReq, zoneConfig);
        } catch (VCertException e) {
            throw new AbortException("Error requesting certificate from VCert "
                + connectorConfig.getType() + ": "
                + e.getMessage());
        }

        return certReq;
    }

    private PEMCollection retrieveCertificate(ConnectorConfig connectorConfig, VCertClient client,
        CertificateRequest certReq) throws AbortException
    {
        try {
            return client.retrieveCertificate(certReq);
        } catch (VCertException e) {
            throw new AbortException("Error retrieving certificate from VCert "
                + connectorConfig.getType() + ": "
                + e.getMessage());
        }
    }

    private void writeOutputFiles(FilePath workspace, PEMCollection pemCollection)
        throws InterruptedException, IOException
    {
        FilePath privKeyOutputFile = workspace.child(getPrivKeyOutput());
        privKeyOutputFile.write(pemCollection.pemPrivateKey(), "UTF-8");
        privKeyOutputFile.chmod(0600);

        workspace.child(getCertOutput()).write(
            pemCollection.pemCertificate(), "UTF-8");
        workspace.child(getCertChainOutput()).write(
            pemCollection.pemCertificateChain(), "UTF-8");
    }

    @Symbol("venafiVcertRequestCertificate")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.CertRequestBuilder_displayName();
        }

        public ListBoxModel doFillConnectorNameItems() {
            ListBoxModel items = new ListBoxModel();
            for (ConnectorConfig config : PluginConfig.get().getConnectorConfigs()) {
                items.add(config.getName(), config.getName());
            }
            return items;
        }

        public FormValidation doCheckConnectorName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckZoneConfigName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckCommonName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckPrivKeyOutput(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckCertOutput(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckCertChainOutput(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckExpirationWindow(@QueryParameter String value) {
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.ok();
            }

            try {
                int n = Integer.parseInt(value);
                if (n < 0) {
                    return FormValidation.error(Messages.CertRequestBuilder_atLeastZeroNumberRequired());
                }
                return FormValidation.ok();
            } catch (NumberFormatException e) {
                return FormValidation.error(Messages.CertRequestBuilder_invalidNumber());
            }
        }
    }
}
