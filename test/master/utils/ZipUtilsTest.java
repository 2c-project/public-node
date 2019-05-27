package master.utils;

import master.resources.ConfigurationManager;
import master.resources.Configurations;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class ZipUtilsTest {

    @Test
    public void extract() throws IOException {
        ConfigurationManager.initConfigs(null);
        byte[] file = new FileInputStream(Configurations.getStoragePrefix() + "/archives/1_5.zip").readAllBytes();
        ZipUtils.extract(file, Configurations.getStoragePrefix() + "/another/");
    }
}