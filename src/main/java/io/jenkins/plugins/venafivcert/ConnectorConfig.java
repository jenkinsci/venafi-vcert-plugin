package io.jenkins.plugins.venafivcert;

import java.util.Collections;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

@XStreamAlias("connector-config")
public class ConnectorConfig extends AbstractDescribableImpl<ConnectorConfig> {
    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String name;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private ConnectorType type;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String tppBaseUrl;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String tppCredentialsId;

    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    private String cloudCredentialsId;

    @DataBoundConstructor
    public ConnectorConfig() {
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String value) {
        this.name = value;
    }

    public ConnectorType getType() {
        return type;
    }

    @DataBoundSetter
    public void setType(ConnectorType value) {
        this.type = value;
    }

    public String getTppBaseUrl() {
        return tppBaseUrl;
    }

    @DataBoundSetter
    public void setTppBaseUrl(String value) {
        this.tppBaseUrl = value;
    }

    public String getTppCredentialsId() {
        return tppCredentialsId;
    }

    @DataBoundSetter
    public void setTppCredentialsId(String value) {
        this.tppCredentialsId = value;
    }

    public String getCloudCredentialsId() {
        return cloudCredentialsId;
    }

    @DataBoundSetter
    public void setCloudCredentialsId(String value) {
        this.cloudCredentialsId = value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ConnectorConfig> {
        @Override
        public String getDisplayName() {
            return Messages.ConnectorConfig_displayName();
        }

        public ListBoxModel doFillTppCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String value) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(value);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(value);
                }
            }

            return result
                .includeMatchingAs(ACL.SYSTEM,
                    item,
                    StandardCredentials.class,
                    Collections.emptyList(),
                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)
                )
                .includeCurrentValue(value);
        }

        public ListBoxModel doFillCloudCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String value) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(value);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(value);
                }
            }

            return result
                .includeMatchingAs(ACL.SYSTEM,
                    item,
                    StringCredentials.class,
                    Collections.emptyList(),
                    CredentialsMatchers.anyOf(
					    CredentialsMatchers.instanceOf(StringCredentials.class))
                )
                .includeCurrentValue(value);
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckTppBaseUrl(@QueryParameter String value, @QueryParameter String type) {
            ConnectorType cType = ConnectorType.valueOf(type);
            if (cType == ConnectorType.TLS_PROTECT) {
                return FormValidation.validateRequired(value);
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckTppCredentialsId(@AncestorInPath Item item, @QueryParameter String value,
            @QueryParameter ConnectorType type)
        {
            if (type != ConnectorType.TLS_PROTECT) {
                return FormValidation.ok();
            }

            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }

            if (Utils.findCredentials(StandardUsernamePasswordCredentials.class, value, item) == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckCloudCredentialsId(@AncestorInPath Item item, @QueryParameter String value,
            @QueryParameter ConnectorType type)
        {
            if (type != ConnectorType.DEVOPS_ACCELERATE) {
                return FormValidation.ok();
            }

            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }

            if (Utils.findCredentials(StringCredentials.class, value, item) == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }

            return FormValidation.ok();
        }
    }
}
