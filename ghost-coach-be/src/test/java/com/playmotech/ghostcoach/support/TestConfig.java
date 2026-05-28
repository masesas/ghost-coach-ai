package com.playmotech.ghostcoach.support;

import com.playmotech.ghostcoach.config.AppConfigProp;
import org.springframework.util.unit.DataSize;

/**
 * Shared factories for building test doubles of {@link AppConfigProp} so each test
 * doesn't have to wire up the inner records by hand.
 */
public final class TestConfig {

    private TestConfig() {}

    public static AppConfigProp defaultAppConfigProp() {
        AppConfigProp prop = new AppConfigProp();
        AppConfigProp.Storage storage = new AppConfigProp.Storage();
        storage.setUploadDir("./test-uploads");
        storage.setMaxFileSize(DataSize.ofMegabytes(5));
        prop.setStorage(storage);
        return prop;
    }

    public static AppConfigProp appConfigPropWithMaxSize(DataSize maxSize) {
        AppConfigProp prop = defaultAppConfigProp();
        prop.getStorage().setMaxFileSize(maxSize);
        return prop;
    }
}
