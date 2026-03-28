# Concepts Covered

## CO1

- **Classes and Objects**
  - `User`, `Farmer`, `Buyer`, `MatchRecord`, `MarketplaceManager`
- **Arrays**
  - crop list in `AppConstants`
- **Strings**
  - IDs, names, cities, crop types, phone numbers, reports
- **Vectors**
  - table population in `FarmerPanel`, `BuyerPanel`, and `MatchPanel`
- **Operators, Loops, Decision Making**
  - matching score logic, validations, searches, and file-reading loops

## CO2 and CO3

- **Encapsulation**
  - private fields with getters and setters in model classes
- **Constructors**
  - overloaded constructors in `Farmer` and `Buyer`
- **Inheritance**
  - `Farmer` and `Buyer` inherit from abstract `User`
- **Polymorphism**
  - overloading in search methods and constructors
  - overriding in `toString()`, `displayDetails()`, and `toCsv()`
- **Abstraction**
  - abstract `User` class
- **Interfaces**
  - `Persistable`, `Matchable`
- **UML**
  - PlantUML files inside `docs/uml`

## CO4 and CO5

- **File Handling and IO Streams**
  - CSV save/load in `FileManager`
  - object stream backup in `FileManager`
- **Exception Handling**
  - `try-catch` in GUI and persistence workflows
- **Custom Exceptions**
  - `InvalidDataException`
  - `DuplicateIdException`
  - `NoMatchFoundException`
