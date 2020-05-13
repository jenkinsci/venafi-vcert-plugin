package io.jenkins.plugins.venafivcert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.venafi.vcert.sdk.Config;
import com.venafi.vcert.sdk.VCertClient;
import com.venafi.vcert.sdk.VCertException;
import com.venafi.vcert.sdk.Config.ConfigBuilder;
import com.venafi.vcert.sdk.certificate.CertificateRequest;
import com.venafi.vcert.sdk.certificate.KeyType;
import com.venafi.vcert.sdk.certificate.PEMCollection;
import com.venafi.vcert.sdk.connectors.tpp.ZoneConfiguration;
import com.venafi.vcert.sdk.endpoint.Authentication;
import com.venafi.vcert.sdk.endpoint.ConnectorType;

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
    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String connectorName;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String zoneConfigName;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private KeyType keyType;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String dnsNames;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String ipAddresses;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String emailAddresses;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String commonName;

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
    private String privKeyOutput;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String certOutput;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String certChainOutput;

    @DataBoundConstructor
    public CertRequestBuilder() {
    }

    public String getConnectorName() {
        return connectorName;
    }

    @DataBoundSetter
    public void setConnectorName(String value) {
        this.connectorName = value;
    }

    public String getZoneConfigName() {
        return zoneConfigName;
    }

    @DataBoundSetter
    public void setZoneConfigName(String value) {
        this.zoneConfigName = value;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    @DataBoundSetter
    public void setKeyType(KeyType value) {
        this.keyType = value;
    }

    public String getDnsNames() {
        return dnsNames;
    }

    @DataBoundSetter
    public void setDnsNames(String value) {
        this.dnsNames = value;
    }

    public String getIpAddresses() {
        return ipAddresses;
    }

    @DataBoundSetter
    public void setIpAddresses(String value) {
        this.ipAddresses = value;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    @DataBoundSetter
    public void setEmailAddresses(String value) {
        this.emailAddresses = value;
    }

    public String getCommonName() {
        return commonName;
    }

    @DataBoundSetter
    public void setCommonName(String value) {
        this.commonName = value;
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

    @DataBoundSetter
    public void setPrivKeyOutput(String value) {
        this.privKeyOutput = value;
    }

    public String getCertOutput() {
        return certOutput;
    }

    @DataBoundSetter
    public void setCertOutput(String certOutput) {
        this.certOutput = certOutput;
    }

    public String getCertChainOutput() {
        return certChainOutput;
    }

    @DataBoundSetter
    public void setCertChainOutput(String certChainOutput) {
        this.certChainOutput = certChainOutput;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws InterruptedException, IOException
    {
        ConnectorConfig connectorConfig = PluginConfig.get()
            .getConnectorConfigByName(getConnectorName());
        if (connectorConfig == null) {
            throw new AbortException("No Venafi VCert connector configuration with name '"
                + getConnectorName() + "' found");
        }

        try {
            Config sdkConfig = createSdkConfig(connectorConfig);
            Authentication sdkAuth = createSdkAuthObject(run, connectorConfig);
            VCertClient client = new VCertClient(sdkConfig);
            client.authenticate(sdkAuth);

            ZoneConfiguration zoneConfig = client.readZoneConfiguration(getZoneConfigName());

            CertificateRequest certReq = new CertificateRequest();
            certReq
                .keyType(getKeyType())
                .dnsNames(getAsList(getDnsNames()))
                .ipAddresses(getIpAddressesAsInetAddresses())
                .emailAddresses(getAsList(getEmailAddresses()));
            certReq.subject(
                new CertificateRequest.PKIXName()
                    .commonName(getCommonName())
                    .organization(getAsList(getOrganization()))
                    .organizationalUnit(getAsList(getOrganizationalUnit()))
                    .country(getAsList(getCountry()))
                    .locality(getAsList(getLocality()))
                    .province(getAsList(getProvince())));

            certReq = client.generateRequest(zoneConfig, certReq);
            client.requestCertificate(certReq, zoneConfig);

            PEMCollection pemCollection = client.retrieveCertificate(certReq);

            FilePath privKeyOutputFile = workspace.child(getPrivKeyOutput());
            privKeyOutputFile.write(pemCollection.pemPrivateKey(), "UTF-8");
            privKeyOutputFile.chmod(0600);

            workspace.child(getCertOutput()).write(
                pemCollection.pemCertificate(), "UTF-8");
            workspace.child(getCertChainOutput()).write(
                pemCollection.pemCertificateChain(), "UTF-8");
        } catch (VCertException e) {
            throw new AbortException("VCert error: " + e.getMessage());
        }
    }

    private List<String> getAsList(String value) {
        if (value == null) {
            return Collections.emptyList();
        } else {
            return Utils.parseStringAsNewlineDelimitedList(value);
        }
    }

    private Collection<InetAddress> getIpAddressesAsInetAddresses()
        throws AbortException
    {
        try {
            Collection<InetAddress> result = new ArrayList<InetAddress>();
            for (String ipAddr: getAsList(getIpAddresses())) {
                result.add(InetAddress.getByName(ipAddr));
            }
            return result;
        } catch (UnknownHostException e) {
            throw new AbortException("Error resolving one of the provided IP addresses: "
                + e.getMessage());
        }
    }

    private Config createSdkConfig(ConnectorConfig connectorConfig) {
        ConfigBuilder sdkConfig = Config.builder();
        sdkConfig.connectorType(connectorConfig.getType());
        if (connectorConfig.getType() == ConnectorType.TPP) {
            sdkConfig.baseUrl(connectorConfig.getTppBaseUrl());
        }
        return sdkConfig.build();
    }

    private Authentication createSdkAuthObject(Run<?, ?> run, ConnectorConfig connectorConfig) {
        if (connectorConfig.getType() == ConnectorType.TPP) {
            StandardUsernamePasswordCredentials credentials = Utils.findCredentials(
                StandardUsernamePasswordCredentials.class,
                connectorConfig.getTppCredentialsId(),
                null);
            CredentialsProvider.track(run, credentials);
            return Authentication.builder()
                .user(credentials.getUsername())
                .password(Secret.toString(credentials.getPassword()))
                .build();
        } else {
            assert connectorConfig.getType() == ConnectorType.CLOUD;
            StringCredentials credentials = Utils.findCredentials(
                StringCredentials.class,
                connectorConfig.getCloudCredentialsId(),
                null);
            CredentialsProvider.track(run, credentials);
            return Authentication.builder()
                .apiKey(Secret.toString(credentials.getSecret()))
                .build();
        }
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

        public FormValidation doCheckZoneConfig(@QueryParameter String value) {
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
    }
}
