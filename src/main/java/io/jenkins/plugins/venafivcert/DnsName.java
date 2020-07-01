package io.jenkins.plugins.venafivcert;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

@XStreamAlias("dns-name")
public class DnsName extends AbstractDescribableImpl<DnsName> {
    private final String hostName;

    @DataBoundConstructor
    public DnsName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DnsName> {
        public FormValidation doCheckHostName(@QueryParameter String value)
        {
            return FormValidation.validateRequired(value);
        }
    }
}
