package farmmarket.interfaces;

import farmmarket.model.Buyer;

public interface Matchable {
    boolean isCompatibleWith(Buyer buyer);

    double calculateMatchScore(Buyer buyer);
}
