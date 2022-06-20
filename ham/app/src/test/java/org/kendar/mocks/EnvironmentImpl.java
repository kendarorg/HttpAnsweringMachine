package org.kendar.mocks;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

public class EnvironmentImpl implements Environment {
    @Override
    public String[] getActiveProfiles() {
        return new String[0];
    }

    @Override
    public String[] getDefaultProfiles() {
        return new String[0];
    }

    @Deprecated
    @Override
    public boolean acceptsProfiles(String... strings) {
        return false;
    }


    @Override
    public boolean acceptsProfiles(Profiles profiles) {
        return false;
    }

    @Override
    public boolean containsProperty(String s) {
        return false;
    }

    @Override
    public String getProperty(String s) {
        return null;
    }

    @Override
    public String getProperty(String s, String s1) {
        return null;
    }

    @Override
    public <T> T getProperty(String s, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T getProperty(String s, Class<T> aClass, T t) {
        return null;
    }

    @Override
    public String getRequiredProperty(String s) throws IllegalStateException {
        return null;
    }

    @Override
    public <T> T getRequiredProperty(String s, Class<T> aClass) throws IllegalStateException {
        return null;
    }

    @Override
    public String resolvePlaceholders(String s) {
        return null;
    }

    @Override
    public String resolveRequiredPlaceholders(String s) throws IllegalArgumentException {
        return null;
    }
}
