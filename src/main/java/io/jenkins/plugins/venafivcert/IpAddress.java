package io.jenkins.plugins.venafivcert;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

@XStreamAlias("dns-name")
public class IpAddress extends AbstractDescribableImpl<IpAddress> {
    private final String address;

    @DataBoundConstructor
    public IpAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<IpAddress> {
        public FormValidation doCheckAddress(@QueryParameter String value)
        {
            return FormValidation.validateRequired(value);
        }
    }
}
