package master.configs;


import master.resources.ConfigurationManager;
import master.resources.Configurations;
import master.services.LifecycleThread;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Runner {
    private static LifecycleThread lifecycleThread;

    public static void main(String[] args) {
        configure(args);
        System.out.println("Loaded properties: " + ConfigurationManager.getAllConfigs());
        System.out.println("Configuration ended, starts syncing;");
        sync();
        System.out.println("Synchronization finished, blockchain values: height=" + LifecycleProperties.getBlockchainHeight() + ", hash=" + LifecycleProperties.getLastBlockHash() + ", timestamp=" + LifecycleProperties.getLastBlockTime());
        registerNode();
        System.out.println("Registering node.. Neighbours amount " + LifecycleProperties.getNeighbours().size());
        startLifecycle();
        System.out.println("Lifecycle started? " + (lifecycleThread != null ? "Yes" : "No"));
        setShutdownHook();
        System.out.println("Shutdown hook set");
        WebServer.startServer();
        System.out.println(LifecycleProperties.getBlockchainHeight());
    }

    private static void configure(String[] args) {
        String externalConfig = null;
        String[] profiles = null;
        for (String arg : args) {
            if (arg.startsWith("external=")) externalConfig = arg.substring(9);
            if (arg.startsWith("profiles=")) profiles = arg.substring(9).split(",");
        }
        ConfigurationManager.initConfigs(externalConfig, profiles);
        try {
            Files.writeString(Path.of("./node.pid"), String.valueOf(ProcessHandle.current().pid()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("Can't set up .pid: [" + e.getClass() + "] " + e.getMessage());
        }

        LifecycleProperties.loadParams();
    }

    private static void sync() {

    }

    private static void registerNode() {

    }

    private static void startLifecycle() {
        String participantAddress = Configurations.getParticipantAddress();
        if (participantAddress != null) {
            lifecycleThread = new LifecycleThread(participantAddress);
            lifecycleThread.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> lifecycleThread.finish()));
        }
    }

    private static void setShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileOutputStream fileOut =
                        new FileOutputStream(Configurations.getStoragePrefix() + "/lastTransactions.ser");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(LifecycleProperties.lastTransactions);
                out.close();
                fileOut.close();
                System.out.println("Persisted latest transactions");
            } catch (Exception e) {
                System.err.println("Can't persist latest transactions: [" + e.getClass() + "] " + e.getMessage());
            }
        }));
    }
}
