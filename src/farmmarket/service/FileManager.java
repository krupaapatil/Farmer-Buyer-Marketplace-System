package farmmarket.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.model.Buyer;
import farmmarket.model.Farmer;
import farmmarket.model.MarketplaceBackup;
import farmmarket.model.MatchRecord;
import farmmarket.util.AppConstants;

public class FileManager {
    private final Path dataDirectory;
    private final Path farmersFile;
    private final Path buyersFile;
    private final Path matchesFile;
    private final Path backupFile;

    public FileManager() {
        this(Paths.get(AppConstants.DATA_DIRECTORY));
    }

    public FileManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.farmersFile = dataDirectory.resolve(AppConstants.FARMERS_FILE);
        this.buyersFile = dataDirectory.resolve(AppConstants.BUYERS_FILE);
        this.matchesFile = dataDirectory.resolve(AppConstants.MATCHES_FILE);
        this.backupFile = dataDirectory.resolve(AppConstants.BACKUP_FILE);
    }

    public Path getFarmersFile() {
        return farmersFile;
    }

    public Path getBuyersFile() {
        return buyersFile;
    }

    public Path getMatchesFile() {
        return matchesFile;
    }

    public Path getBackupFile() {
        return backupFile;
    }

    public void saveFarmers(List<Farmer> farmers) throws IOException {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(farmersFile.toFile()))) {
            writer.write("id,name,city,phone,cropType,quantityAvailable,pricePerUnit");
            writer.newLine();
            for (Farmer farmer : farmers) {
                writer.write(farmer.toCsv());
                writer.newLine();
            }
        }
    }

    public void saveBuyers(List<Buyer> buyers) throws IOException {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(buyersFile.toFile()))) {
            writer.write("id,name,city,phone,requiredCrop,requiredQuantity,maxBudget");
            writer.newLine();
            for (Buyer buyer : buyers) {
                writer.write(buyer.toCsv());
                writer.newLine();
            }
        }
    }

    public void saveMatches(List<MatchRecord> matches) throws IOException {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(matchesFile.toFile()))) {
            writer.write("buyerId,buyerName,farmerId,farmerName,cropType,city,availableQuantity,pricePerUnit,score,status");
            writer.newLine();
            for (MatchRecord match : matches) {
                writer.write(match.toCsv());
                writer.newLine();
            }
        }
    }

    public List<Farmer> loadFarmers() throws IOException, InvalidDataException {
        ensureDataDirectory();
        List<Farmer> farmers = new ArrayList<>();
        if (!Files.exists(farmersFile)) {
            return farmers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(farmersFile.toFile()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("id,")) {
                        continue;
                    }
                }
                String[] values = line.split(",", -1);
                if (values.length != 7) {
                    throw new InvalidDataException("Invalid farmer record: " + line);
                }
                try {
                    Farmer farmer = new Farmer(
                            values[0],
                            values[1],
                            values[2],
                            values[3],
                            values[4],
                            Integer.parseInt(values[5]),
                            Double.parseDouble(values[6]));
                    farmers.add(farmer);
                } catch (NumberFormatException e) {
                    throw new InvalidDataException("Invalid number in farmer record: " + line);
                }
            }
        }
        return farmers;
    }

    public List<Buyer> loadBuyers() throws IOException, InvalidDataException {
        ensureDataDirectory();
        List<Buyer> buyers = new ArrayList<>();
        if (!Files.exists(buyersFile)) {
            return buyers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(buyersFile.toFile()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("id,")) {
                        continue;
                    }
                }
                String[] values = line.split(",", -1);
                if (values.length != 7) {
                    throw new InvalidDataException("Invalid buyer record: " + line);
                }
                try {
                    Buyer buyer = new Buyer(
                            values[0],
                            values[1],
                            values[2],
                            values[3],
                            values[4],
                            Integer.parseInt(values[5]),
                            Double.parseDouble(values[6]));
                    buyers.add(buyer);
                } catch (NumberFormatException e) {
                    throw new InvalidDataException("Invalid number in buyer record: " + line);
                }
            }
        }
        return buyers;
    }

    public void exportBackup(List<Farmer> farmers, List<Buyer> buyers, List<MatchRecord> matches) throws IOException {
        ensureDataDirectory();
        MarketplaceBackup backup = new MarketplaceBackup(farmers, buyers, matches);
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(backupFile.toFile()))) {
            outputStream.writeObject(backup);
        }
    }

    public MarketplaceBackup importBackup() throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(backupFile.toFile()))) {
            return (MarketplaceBackup) inputStream.readObject();
        }
    }

    private void ensureDataDirectory() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
    }
}
