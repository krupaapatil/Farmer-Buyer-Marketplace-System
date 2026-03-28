# Test Cases

## Functional Checks

1. Add a farmer with valid values and confirm the record appears in the Farmers table.
2. Add a buyer with valid values and confirm the record appears in the Buyers table.
3. Search farmers by crop only and verify filtered results.
4. Search farmers by crop and city and verify filtered results.
5. Search buyers by crop and city and verify filtered results.
6. Generate matches for a selected buyer and verify best score appears first.
7. Generate all matches and verify only compatible city and price combinations appear.
8. Export matches and confirm `data/matches.csv` updates.
9. Save all records, restart the app, and confirm the data loads again.
10. Create a backup and restore it successfully.

## Validation Checks

1. Leave name or city blank and verify an error dialog appears.
2. Enter negative quantity and verify an error dialog appears.
3. Enter negative price or budget and verify an error dialog appears.
4. Reuse an existing farmer or buyer ID and verify `DuplicateIdException` behavior.
5. Corrupt a CSV number value manually and verify load shows a file/data error.

## Matching Scenarios

1. Exact crop, same city, acceptable price:
   - should generate a match
2. Crop matches but city differs:
   - should not generate a match
3. Crop matches and city matches but price exceeds budget:
   - should not generate a match
4. No compatible farmer exists:
   - should show a no-match message
