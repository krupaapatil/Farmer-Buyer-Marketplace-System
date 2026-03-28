package farmmarket.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.exceptions.NoMatchFoundException;
import farmmarket.model.Buyer;
import farmmarket.model.Farmer;
import farmmarket.model.MarketplaceBackup;
import farmmarket.model.MatchRecord;
import farmmarket.util.ReportGenerator;
import farmmarket.util.ValidationUtil;

public class MarketplaceManager {
    private final List<Farmer> farmers;
    private final List<Buyer> buyers;
    private final List<MatchRecord> lastGeneratedMatches;

    public MarketplaceManager() {
        this.farmers = new ArrayList<>();
        this.buyers = new ArrayList<>();
        this.lastGeneratedMatches = new ArrayList<>();
    }

    public void addFarmer(Farmer farmer) throws DuplicateIdException {
        ensureUniqueId(farmer.getId());
        farmers.add(farmer);
    }

    public void addBuyer(Buyer buyer) throws DuplicateIdException {
        ensureUniqueId(buyer.getId());
        buyers.add(buyer);
    }

    public List<Farmer> getFarmers() {
        return new ArrayList<>(farmers);
    }

    public List<Buyer> getBuyers() {
        return new ArrayList<>(buyers);
    }

    public List<MatchRecord> getLastGeneratedMatches() {
        return new ArrayList<>(lastGeneratedMatches);
    }

    public List<Farmer> searchFarmers(String cropType) {
        return searchFarmers(cropType, "");
    }

    public List<Farmer> searchFarmers(String cropType, String city) {
        List<Farmer> results = new ArrayList<>();
        for (Farmer farmer : farmers) {
            boolean matchesCrop = ValidationUtil.normalizeText(cropType).isEmpty()
                    || ValidationUtil.matchesIgnoreCase(farmer.getCropType(), cropType);
            boolean matchesCity = ValidationUtil.normalizeText(city).isEmpty()
                    || ValidationUtil.matchesIgnoreCase(farmer.getCity(), city);
            if (matchesCrop && matchesCity) {
                results.add(farmer);
            }
        }
        return results;
    }

    public List<Buyer> searchBuyers(String cropType) {
        return searchBuyers(cropType, "");
    }

    public List<Buyer> searchBuyers(String cropType, String city) {
        List<Buyer> results = new ArrayList<>();
        for (Buyer buyer : buyers) {
            boolean matchesCrop = ValidationUtil.normalizeText(cropType).isEmpty()
                    || ValidationUtil.matchesIgnoreCase(buyer.getRequiredCrop(), cropType);
            boolean matchesCity = ValidationUtil.normalizeText(city).isEmpty()
                    || ValidationUtil.matchesIgnoreCase(buyer.getCity(), city);
            if (matchesCrop && matchesCity) {
                results.add(buyer);
            }
        }
        return results;
    }

    public List<MatchRecord> generateMatchesForBuyer(String buyerId) throws NoMatchFoundException {
        Buyer buyer = findBuyerById(buyerId)
                .orElseThrow(() -> new NoMatchFoundException("Buyer not found for ID: " + buyerId));
        return generateMatchesForBuyer(buyer);
    }

    public List<MatchRecord> generateMatchesForBuyer(Buyer buyer) throws NoMatchFoundException {
        List<MatchRecord> matches = new ArrayList<>();
        for (Farmer farmer : farmers) {
            if (farmer.isCompatibleWith(buyer)) {
                double score = farmer.calculateMatchScore(buyer);
                matches.add(new MatchRecord(buyer, farmer, score, "Recommended"));
            }
        }

        if (matches.isEmpty()) {
            throw new NoMatchFoundException("No compatible farmer found for buyer " + buyer.getName() + ".");
        }

        Collections.sort(matches);
        lastGeneratedMatches.clear();
        lastGeneratedMatches.addAll(matches);
        return matches;
    }

    public List<MatchRecord> generateAllMatches() throws NoMatchFoundException {
        List<MatchRecord> matches = new ArrayList<>();
        for (Buyer buyer : buyers) {
            for (Farmer farmer : farmers) {
                if (farmer.isCompatibleWith(buyer)) {
                    double score = farmer.calculateMatchScore(buyer);
                    matches.add(new MatchRecord(buyer, farmer, score, "Recommended"));
                }
            }
        }

        if (matches.isEmpty()) {
            throw new NoMatchFoundException("No matches available for the current data.");
        }

        matches.sort(Comparator.naturalOrder());
        lastGeneratedMatches.clear();
        lastGeneratedMatches.addAll(matches);
        return new ArrayList<>(matches);
    }

    public Optional<Buyer> findBuyerById(String buyerId) {
        for (Buyer buyer : buyers) {
            if (buyer.getId().equalsIgnoreCase(buyerId)) {
                return Optional.of(buyer);
            }
        }
        return Optional.empty();
    }

    public void saveAll(FileManager fileManager) throws IOException {
        fileManager.saveFarmers(farmers);
        fileManager.saveBuyers(buyers);
        fileManager.saveMatches(lastGeneratedMatches);
    }

    public void loadAll(FileManager fileManager) throws IOException, InvalidDataException, DuplicateIdException {
        List<Farmer> loadedFarmers = fileManager.loadFarmers();
        List<Buyer> loadedBuyers = fileManager.loadBuyers();

        farmers.clear();
        buyers.clear();
        lastGeneratedMatches.clear();

        for (Farmer farmer : loadedFarmers) {
            addFarmer(farmer);
        }
        for (Buyer buyer : loadedBuyers) {
            addBuyer(buyer);
        }
    }

    public void exportBackup(FileManager fileManager) throws IOException {
        fileManager.exportBackup(farmers, buyers, lastGeneratedMatches);
    }

    public void importBackup(FileManager fileManager) throws IOException, ClassNotFoundException, DuplicateIdException {
        MarketplaceBackup backup = fileManager.importBackup();

        farmers.clear();
        buyers.clear();
        lastGeneratedMatches.clear();

        for (Farmer farmer : backup.getFarmers()) {
            addFarmer(farmer);
        }
        for (Buyer buyer : backup.getBuyers()) {
            addBuyer(buyer);
        }
        lastGeneratedMatches.addAll(backup.getMatches());
    }

    public String buildSummaryReport() {
        return ReportGenerator.generateSummary(farmers, buyers, lastGeneratedMatches);
    }

    public String buildMatchReport() {
        return ReportGenerator.generateMatchesReport(lastGeneratedMatches);
    }

    private void ensureUniqueId(String id) throws DuplicateIdException {
        for (Farmer farmer : farmers) {
            if (farmer.getId().equalsIgnoreCase(id)) {
                throw new DuplicateIdException("Duplicate ID found: " + id);
            }
        }
        for (Buyer buyer : buyers) {
            if (buyer.getId().equalsIgnoreCase(id)) {
                throw new DuplicateIdException("Duplicate ID found: " + id);
            }
        }
    }
}
