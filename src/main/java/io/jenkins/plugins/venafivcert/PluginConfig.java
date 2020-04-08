package io.jenkins.plugins.venafivcert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.XmlFile;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class PluginConfig extends GlobalConfiguration {
    public static final String CONFIGURATION_ID = "venafi-vcert-plugin-configuration";
    public static final PluginConfig EMPTY_CONFIG = new PluginConfig(Collections.emptyList());

    private List<ConnectorConfig> connectorConfigs = new ArrayList<>();

    @Nonnull
    public static PluginConfig get() {
        PluginConfig result = PluginConfig.all().get(PluginConfig.class);
        if (result == null) {
            return PluginConfig.EMPTY_CONFIG;
        } else {
            return result;
        }
    }

    public PluginConfig() {
        load();
    }

    public PluginConfig(List<ConnectorConfig> connectorConfigs) {
        this.connectorConfigs = connectorConfigs;
    }

    @Nonnull
    public List<ConnectorConfig> getConnectorConfigs() {
        return connectorConfigs;
    }

    @Nullable
    public ConnectorConfig getTppConfigByName(String name) {
        for (ConnectorConfig config: connectorConfigs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
    }

    @DataBoundSetter
    public void setConnectorConfigs(List<ConnectorConfig> value) {
        this.connectorConfigs = value;
    }

    @Override
    public String getId() {
        return CONFIGURATION_ID;
    }

    @Override
    protected XmlFile getConfigFile() {
        return new XmlFile(Jenkins.XSTREAM2, super.getConfigFile().getFile());
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            req.bindJSON(this, json);
        } catch (Exception e) {
            throw new FormException(
                String.format(Messages.PluginConfig_malformedError(), e.getMessage()),
                e, CONFIGURATION_ID);
        }

        String duplicateName = findDuplicateConnectorConfigName(connectorConfigs);
        if (duplicateName != null) {
            throw new FormException(
                String.format(Messages.PluginConfig_duplicateConnectorConfigName(), duplicateName),
                CONFIGURATION_ID);
        }

        save();
        return true;
    }

    @Override
    public String getDisplayName() {
        return Messages.PluginConfig_displayName();
    }

    private String findDuplicateConnectorConfigName(List<ConnectorConfig> connectorConfigs) {
        Set<String> namesSeen = new HashSet<>();
        for (ConnectorConfig config: connectorConfigs) {
            if (!namesSeen.add(config.getName())) {
                return config.getName();
            }
        }
        return null;
    }
}
