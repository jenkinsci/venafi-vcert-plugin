package io.jenkins.plugins.venafivcert;

import java.util.ArrayList;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
    private String cloudApiKey;

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

    public ConnectorType getTypeObject() {
        return type;
    }

    public String getType() {
        return type.toString();
    }

    @DataBoundSetter
    public void setType(String displayName) {
        this.type = ConnectorType.valueOfDisplayName(displayName);
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

    public String getCloudApiKey() {
        return cloudApiKey;
    }

    @DataBoundSetter
    public void setCloudApiKey(String value) {
        this.cloudApiKey = value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ConnectorConfig> {
        @Override
        public String getDisplayName() {
            return Messages.ConnectorConfig_displayName();
        }

        public ListBoxModel doFillTypeItems() {
            ListBoxModel items = new ListBoxModel();
            for (ConnectorType type: ConnectorType.class.getEnumConstants()) {
                items.add(type.toString(), type.name());
            }
            return items;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }

            return result
                .includeMatchingAs(ACL.SYSTEM,
                    item,
                    StandardCredentials.class,
                    new ArrayList<>(),
                    CredentialsMatchers.anyOf(
					    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
					    CredentialsMatchers.instanceOf(UsernamePasswordCredentials.class))
                )
                .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter String id) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String credentialsId) {
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

            if (Utils.findCredentials(credentialsId, item) == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }

            return FormValidation.ok();
        }
    }
}
