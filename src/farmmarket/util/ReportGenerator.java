package farmmarket.util;

import java.util.List;

import farmmarket.model.Buyer;
import farmmarket.model.Farmer;
import farmmarket.model.MatchRecord;

public final class ReportGenerator {
    private ReportGenerator() {
    }

    public static String generateSummary(List<Farmer> farmers, List<Buyer> buyers, List<MatchRecord> matches) {
        StringBuilder builder = new StringBuilder();
        builder.append("Farmer-Buyer Marketplace Summary").append(System.lineSeparator());
        builder.append("================================").append(System.lineSeparator());
        builder.append("Total Farmers: ").append(farmers.size()).append(System.lineSeparator());
        builder.append("Total Buyers: ").append(buyers.size()).append(System.lineSeparator());
        builder.append("Last Generated Matches: ").append(matches.size()).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Crop Categories Available").append(System.lineSeparator());
        for (String cropType : AppConstants.CROP_TYPES) {
            builder.append("- ").append(cropType).append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
        builder.append("Farmers").append(System.lineSeparator());
        if (farmers.isEmpty()) {
            builder.append("No farmer records loaded.").append(System.lineSeparator());
        } else {
            for (Farmer farmer : farmers) {
                builder.append(farmer.displayDetails()).append(System.lineSeparator());
            }
        }

        builder.append(System.lineSeparator());
        builder.append("Buyers").append(System.lineSeparator());
        if (buyers.isEmpty()) {
            builder.append("No buyer records loaded.").append(System.lineSeparator());
        } else {
            for (Buyer buyer : buyers) {
                builder.append(buyer.displayDetails()).append(System.lineSeparator());
            }
        }

        builder.append(System.lineSeparator());
        builder.append("Matches").append(System.lineSeparator());
        if (matches.isEmpty()) {
            builder.append("No matches generated yet.").append(System.lineSeparator());
        } else {
            for (MatchRecord match : matches) {
                builder.append(match.toCsv()).append(System.lineSeparator());
            }
        }

        return builder.toString();
    }

    public static String generateMatchesReport(List<MatchRecord> matches) {
        StringBuilder builder = new StringBuilder();
        builder.append("Match Report").append(System.lineSeparator());
        builder.append("============").append(System.lineSeparator());

        if (matches.isEmpty()) {
            builder.append("No matches available.");
            return builder.toString();
        }

        int rank = 1;
        for (MatchRecord match : matches) {
            builder.append(rank)
                    .append(". ")
                    .append(match.toCsv())
                    .append(System.lineSeparator());
            rank++;
        }

        return builder.toString();
    }
}
