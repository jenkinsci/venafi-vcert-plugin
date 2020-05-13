package io.jenkins.plugins.venafivcert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static List<String> parseStringAsNewlineDelimitedList(String input) {
        List<String> result = new ArrayList<String>();
        for (String line: input.split("\\s+")) {
            line = line.trim();
            if (!line.isEmpty()) {
                result.add(line);
            }
        }
        return result;
    }
}
