package master.storage;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import master.configs.LifecycleProperties;
import master.exceptions.BlockNotExistsException;
import master.exceptions.InvalidTransactionException;
import master.models.Address;
import master.models.Block;
import master.models.Transaction;
import master.resources.Configurations;
import master.utils.FileUtils;
import master.utils.Utils;
import master.utils.ZipUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class FilesystemStorage {
    private static final int PAGE_SIZE = 50;

    public static Address getWallet(String address) {
        return ObjectMapper.stringToAddress(readFromFilesystem(Configurations.getStoragePrefix() + "wallets/" + address));
    }

    public static Transaction getTransaction(String hash) {
        try {
            return ObjectMapper.stringToTransaction(readFromFilesystem(Configurations.getStoragePrefix() + "transactions/" + hash), hash);
        } catch (Exception e) {
            throw new InvalidTransactionException("Can't map transaction: [" + e.getClass() + "] " + e.getMessage());
        }
    }

    public static Block getBlock(String hash) {
        Path path = FileUtils.getFirstFileByRegexp(Configurations.getStoragePrefix() + "blocks/", "*_" + hash);
        if (path == null) throw new BlockNotExistsException();
        String fileContent = readFromFilesystem(path);
        String filename = path.getFileName().toString();
        return ObjectMapper.stringToBlock(fileContent, hash, Long.parseLong(filename.substring(0, filename.indexOf("_"))));
    }

    public static Block getBlock(long height) {
        if (height == 0) return loadGenesisBlock();
        Path path = FileUtils.getFirstFileByRegexp(Configurations.getStoragePrefix() + "blocks/", height + "_*");
        if (path == null) throw new BlockNotExistsException();
        String fileContent = readFromFilesystem(path);
        String filename = path.getFileName().toString();
        return ObjectMapper.stringToBlock(fileContent, filename.substring(filename.indexOf("_") + 1), height);
    }

    public static JSONObject getLastTransactionsByAddress(String address, int page) {
        JSONObject result = new JSONObject();

        // Path path = Path.of("/home/andrii/test" + "/links/" + address);
        Path path = Path.of(Configurations.getStoragePrefix() + "/links/" + address);

        if (!Files.exists(path)) {
            result.put("page", 0);
            result.put("data", List.of());
            result.put("lastPage", 0);
            result.put("totalItems", 0);
            return result;
        }
        String[] txHashes = readFromFilesystem(path).split("\n");
        Collections.reverse(Arrays.asList(txHashes));

        int from = page * PAGE_SIZE;
        int to = from + PAGE_SIZE <= txHashes.length ? from + PAGE_SIZE : txHashes.length;
        result.put("page", page);
        result.put("lastPage", txHashes.length / PAGE_SIZE);
        result.put("totalItems", txHashes.length);
        return result.put("data", List.of(txHashes).subList(from, to));
    }


    public static Block getBlock(String hash, long height) {
        try {
            return ObjectMapper.stringToBlock(readFromFilesystem(Configurations.getStoragePrefix() + "blocks/" + height + "_" + hash), hash, height);
        } catch (RuntimeException e) {
            throw new BlockNotExistsException();
        }
    }

    public static boolean validateBlock(String hash, long height) {
        return Files.exists(Path.of(Configurations.getStoragePrefix() + "blocks/" + height + "_" + hash));
    }

    public static void saveWallet(Address address) {
        saveToFilesystem(Configurations.getStoragePrefix() + "wallets/" + address.getAddress(), Utils.bytesToHexString(address.getPublicKey()) + '\n' + Utils.bytesToHexString(address.getSecret()));
    }

    public static void saveTransaction(Transaction transaction) {
        saveToFilesystem(Configurations.getStoragePrefix() + "transactions/" + transaction.getHash(), ObjectMapper.transactionToString(transaction));
        addTransactionToAddress(transaction);
    }

    public static void addTransactionToAddress(Transaction transaction) {
        if (!Configurations.getCreateLinks()) return;
        if (transaction.getFrom() != null)
            appendToFilesystem(Configurations.getStoragePrefix() + "links/" + transaction.getFrom(), transaction.getHash() + "\n");
        if (transaction.getTo() != null)
            appendToFilesystem(Configurations.getStoragePrefix() + "links/" + transaction.getTo(), transaction.getHash() + "\n");
    }

    public static void saveBlock(Block block) {
        saveToFilesystem(Configurations.getStoragePrefix() + "blocks/" + block.getHeight() + "_" + block.getHash(), ObjectMapper.blockToString(block));
    }

    public static void deleteBlock(long blockNumber) {
        File dir = new File(Configurations.getStoragePrefix() + "blocks/");

        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(blockNumber + "_"))
                file.delete();
        }
    }

    public static void saveConfigs() {
        saveToFilesystemTruncate(Configurations.getStoragePrefix() + "data.configs", LifecycleProperties.getBlockchainHeight() + "," + LifecycleProperties.getLastBlockHash() + "," + LifecycleProperties.getLastBlockTime());
    }

    public static void loadConfigs() {
        try {
            String fileContent = readFromFilesystem(Configurations.getStoragePrefix() + "data.configs");
            if (fileContent == null) return;
            String[] content = fileContent.split(",");
            LifecycleProperties.updateLast(content[1], Long.parseLong(content[2].strip()), Long.parseLong(content[0].strip()));
        } catch (RuntimeException e) {
            System.out.println("Can't read configs from file [" + Configurations.getStoragePrefix() + "data.configs], got exception: " + e.getClass() + " [" + e.getMessage() + "]");
        }
    }

    public static void saveGenesisBlock(Block block) {
        saveToFilesystem(Configurations.getStoragePrefix() + "blocks/genesis", String.join(",", block.getTransactions()));
    }

    public static Block loadGenesisBlock() {
        Path path = Path.of(Configurations.getStoragePrefix() + "blocks/genesis");
        if (!Files.exists(path)) return null;
        String[] content = readFromFilesystem(path).split("\n");
        return new Block(List.of(content[0].split(",")), null, "0", 0, 0);
    }

    private static void saveToFilesystem(String path, String content) {
        saveToFilesystem(Path.of(path), content);
    }

    private static String readFromFilesystem(String pathStr) {
        return readFromFilesystem(Path.of(pathStr));
    }

    private static void saveToFilesystem(Path path, String content) {
        try {
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new RuntimeException("Can't write to file " + path.toString(), e);
        }
    }

    private static void saveToFilesystemTruncate(String path, String content) {
        saveToFilesystemTruncate(Path.of(path), content);
    }


    private static void saveToFilesystemTruncate(Path path, String content) {
        try {
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Can't write to file " + path.toString(), e);
        }
    }

    private static void appendToFilesystem(String path, String content) {
        appendToFilesystem(Path.of(path), content);
    }

    private static void appendToFilesystem(Path path, String content) {
        try {
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Can't write to file " + path.toString(), e);
        }
    }

    private static String readFromFilesystem(Path pathStr) {
        try {
            CharsetMatch cm;
            try (InputStream input = Files.newInputStream(pathStr)) {
                BufferedInputStream bis = new BufferedInputStream(input);
                CharsetDetector cd = new CharsetDetector();
                cd.setText(bis);
                cm = cd.detect();
            } catch (IOException e) {
                Thread.sleep(2000L);
                return readFromFilesystem(pathStr, true);
            }
            return Files.readString(pathStr, Charset.forName(cm.getName()));
        } catch (Exception e) {
            throw new RuntimeException("Can't read file " + pathStr, e);
        }
    }

    private static String readFromFilesystem(Path pathStr, boolean retry) {
        try {
            CharsetMatch cm;
            try (InputStream input = Files.newInputStream(pathStr)) {
                BufferedInputStream bis = new BufferedInputStream(input);
                CharsetDetector cd = new CharsetDetector();
                cd.setText(bis);
                cm = cd.detect();
            } catch (IOException e) {
                if (retry) throw new RuntimeException(e);
                else return readFromFilesystem(pathStr, true);
            }
            return Files.readString(pathStr, Charset.forName(cm.getName()));
        } catch (Exception e) {
            throw new RuntimeException("Can't read file " + pathStr, e);
        }
    }

    public static String getZipped(long from, long to) {
        if (to > LifecycleProperties.getBlockchainHeight())
            throw new RuntimeException("Invalid to parameter: to=" + to + ", blockchain height = " + LifecycleProperties.getBlockchainHeight());
        String path = Configurations.getStoragePrefix() + "/archives/" + from + "_" + to + ".zip";
        File file = new File(path);
        Path parent = Path.of(file.getParent());
        if (file.exists()) return path;
        try {
            if (!Files.exists(parent)) Files.createDirectories(parent);
            if (!file.createNewFile()) throw new RuntimeException("Can't create file: " + file.toString());
        } catch (IOException e) {
            throw new RuntimeException("Unhandled unexpected file utils exception: " + e.getClass() + " [" + e.getMessage() + "]", e);
        }
        try (FileOutputStream fos = new FileOutputStream(path); ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (long i = from; i <= to; i++) {
                Block targetBlock = getBlock(i);
                String blockName = i == 0 ? "genesis" : i + "_" + targetBlock.getHash();
                ZipUtils.zipFile(new File(Configurations.getStoragePrefix() + "/blocks/" + blockName), "blocks/", zos);
                for (String transaction : targetBlock.getTransactions()) {
                    ZipUtils.zipFile(new File(Configurations.getStoragePrefix() + "/transactions/" + transaction), "transactions/", zos);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unhandled unexpected file utils exception: " + e.getClass() + " [" + e.getMessage() + "]", e);
        }
        return path;
    }

    public static void removeAllLinks() {
        try {
            Files.walk(Path.of(Configurations.getStoragePrefix() + "/links")).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException e) {
            System.err.println("Can't delete links folder, got exception: " + e.getMessage());
        }
    }

    public static void removeOldBlockchain() {
        try {
            Files.walk(Path.of(Configurations.getStoragePrefix() + "/blocks")).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {
                }
            });
            Files.walk(Path.of(Configurations.getStoragePrefix() + "/transactions")).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException e) {
            System.err.println("Can't delete old blockhain, got exception: " + e.getMessage());
        }
    }

    public static void saveNeighbours() {
        try {
            Files.writeString(Path.of(Configurations.getStoragePrefix() + "/neighbours.data"), String.join(",", LifecycleProperties.getNeighbours()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception e) {
            System.err.println("Can't save neighbours, got exception: [" + e.getClass() + "] " + e.getMessage());
        }
    }

    public static List<String> loadNeighbours() {
        try {
            Path filepath = Path.of(Configurations.getStoragePrefix() + "/neighbours.data");
            if (!Files.exists(filepath)) {
                System.out.println("Can't load neighbours, file not exists");
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(Files.readString(filepath, StandardCharsets.UTF_8).split(",")));
        } catch (Exception e) {
            System.out.println("Can't load neighbours, got exception: [" + e.getClass() + "] " + e.getMessage());
            return new ArrayList<>();
        }
    }


}
