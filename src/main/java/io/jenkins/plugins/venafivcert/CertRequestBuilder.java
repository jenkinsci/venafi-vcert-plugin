package io.jenkins.plugins.venafivcert;

import java.io.IOException;

import com.venafi.vcert.sdk.certificate.KeyType;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    private String organizationUnit;

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
    public void setZoneConfigName(String zoneConfigName) {
        this.zoneConfigName = zoneConfigName;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    @DataBoundSetter
    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public String getDnsNames() {
        return dnsNames;
    }

    @DataBoundSetter
    public void setDnsNames(String dnsNames) {
        this.dnsNames = dnsNames;
    }

    public String getIpAddresses() {
        return ipAddresses;
    }

    @DataBoundSetter
    public void setIpAddresses(String ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    @DataBoundSetter
    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getCommonName() {
        return commonName;
    }

    @DataBoundSetter
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganization() {
        return organization;
    }

    @DataBoundSetter
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    @DataBoundSetter
    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public String getLocality() {
        return locality;
    }

    @DataBoundSetter
    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getProvince() {
        return province;
    }

    @DataBoundSetter
    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    @DataBoundSetter
    public void setCountry(String country) {
        this.country = country;
    }

    public String getPrivKeyOutput() {
        return privKeyOutput;
    }

    @DataBoundSetter
    public void setPrivKeyOutput(String privKeyOutput) {
        this.privKeyOutput = privKeyOutput;
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
