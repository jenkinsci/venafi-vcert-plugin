package io.jenkins.plugins.venafivcert;

import java.util.Collections;

import javax.annotation.Nullable;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;

import org.apache.commons.lang.StringUtils;

import hudson.model.Item;
import hudson.security.ACL;

public class Utils {
    @Nullable
    public static <C extends IdCredentials> C findCredentials(Class<C> type, String credentialsId, Item item) {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }
        return CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(
                type,
                item,
                ACL.SYSTEM,
                Collections.emptyList()),
            CredentialsMatchers.withId(credentialsId));
    }
}
