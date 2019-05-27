package master.resources;

public class Configurations {
    private static Boolean createLinks = null;

    public static String getStoragePrefix() {
        return ConfigurationManager.getConfig("storagePrefix");
    }

    public static String getParentNode() {
        String address = ConfigurationManager.getConfig("parentNode");
        if (address.equals("null")) return null;
        return address;
    }

    public static int getSyncBlockSize() {
        return (int) ConfigurationManager.getConfigAsLong("syncBlockSize");
    }

    public static String getParticipantAddress() {
        return ConfigurationManager.getConfig("participantAddress");
    }

    public static String getStatsWebhook() {
        return ConfigurationManager.getConfig("statsWebhook");
    }

    public static boolean getCreateLinks() {
        if (createLinks == null) createLinks = "true".equals(ConfigurationManager.getConfig("createLinks"));
        return createLinks;
    }

    public static String getToken() {
        String token = null;
        try {
            return ConfigurationManager.getConfig("token");
        } catch (Exception ignored) {
        }
        return token;
    }

}
