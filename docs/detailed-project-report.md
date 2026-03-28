# Farmer-Buyer Marketplace System

## Detailed Project Report

### 1. Introduction

This project, **Farmer-Buyer Marketplace System**, was developed as an undergraduate Java project to demonstrate both **core Java programming** and **Object-Oriented Programming concepts** in a practical and meaningful way.

The main idea behind the project is simple and socially relevant: farmers often have crops available for sale, while buyers are looking for crops within a certain budget and location. This system acts like a digital bridge between the two. It stores farmer and buyer information, compares their needs and offerings, and recommends the best matches based on:

- crop availability
- budget compatibility
- same-city proximity
- quantity closeness

The project was intentionally designed to be **academic, understandable, and presentable**. Instead of making it overly complicated, the system focuses on clearly demonstrating syllabus topics through a realistic application.

This project now supports:

- a **Java Swing desktop application**
- a **browser-based web version**
- **CSV file storage**
- **serialized backup**
- **UML documentation**

So it is both technically complete and strong from a report/viva point of view.

---

### 2. Problem Statement

Farmers and buyers usually depend on middlemen, manual communication, or scattered marketplaces. Because of this, it can be difficult for:

- farmers to find the right buyers
- buyers to locate suitable sellers quickly
- both parties to compare crop type, budget, and location efficiently

This project solves that problem by creating a small marketplace system that:

1. stores farmer details
2. stores buyer requirements
3. compares both sets of data
4. generates suitable recommendations
5. saves and loads data from files

---

### 3. Objectives of the Project

The main objectives of this project are:

- to build a meaningful Java application based on a real-world idea
- to cover the full undergraduate Java syllabus in one project
- to show clear use of OOP concepts like inheritance, abstraction, interfaces, and polymorphism
- to demonstrate file handling, exception handling, and data persistence
- to provide an easy-to-use interface for demonstration and evaluation

---

### 4. Scope of the Project

The current system supports:

- adding farmer records
- adding buyer records
- viewing all saved records
- searching by crop and city
- generating matches for one buyer or all buyers
- saving data to CSV files
- loading data from CSV files
- exporting match reports
- creating and restoring serialized backups

The project does **not** currently include:

- online payments
- user login and authentication
- database integration
- real GPS distance calculation
- advanced analytics

These were kept outside scope so that the project remains suitable for undergraduate academic evaluation.

---

### 5. Technologies Used

- **Java**
  - main programming language
- **Java Swing**
  - desktop GUI
- **Java HTTP Server**
  - lightweight built-in web server for browser access
- **CSV Files**
  - text-based persistent storage
- **Object Streams**
  - serialized backup storage
- **PlantUML**
  - UML diagram source files
- **PowerShell**
  - project run script

---

### 6. Project Workflow

This section explains the workflow in human terms.

#### 6.1 Farmer workflow

1. A user opens the Farmer section.
2. The user enters:
   - farmer ID
   - name
   - city
   - phone
   - crop type
   - quantity available
   - price per unit
3. The UI creates a `Farmer` object.
4. The object is validated through constructors and setters.
5. The manager checks whether the ID is unique.
6. If valid, the farmer is added to the system.
7. The table refreshes and shows the new farmer.

#### 6.2 Buyer workflow

1. A user opens the Buyer section.
2. The user enters:
   - buyer ID
   - name
   - city
   - phone
   - required crop
   - required quantity
   - maximum budget
3. The UI creates a `Buyer` object.
4. Validation is performed.
5. Duplicate ID checks are performed.
6. If successful, the buyer is stored and displayed.

#### 6.3 Matching workflow

1. The user chooses a buyer or selects the option to generate all matches.
2. The system loops through farmers.
3. For each farmer, the system checks:
   - is the crop the same?
   - is the city the same?
   - is the price within the buyer's budget?
   - is quantity available greater than zero?
4. If compatible, the system calculates a match score.
5. A `MatchRecord` object is created.
6. All compatible matches are sorted by score.
7. The best matches are shown in the table and report area.

#### 6.4 File workflow

1. When the user clicks save, farmer data, buyer data, and match data are written to CSV files.
2. When the user clicks load, the system reads those files line by line.
3. CSV values are converted back into `Farmer` and `Buyer` objects.
4. If there is any invalid data, a custom exception is raised.
5. Backup and restore use object streams to serialize and deserialize the full state.

---

### 7. Package Structure

The project is organized into packages so that each responsibility stays clear.

#### 7.1 `farmmarket.app`

Contains the main starting point of the application.

- `FarmerBuyerMarketplaceApp`

#### 7.2 `farmmarket.model`

Contains all domain classes and data objects.

- `User`
- `Farmer`
- `Buyer`
- `MatchRecord`
- `MarketplaceBackup`

#### 7.3 `farmmarket.interfaces`

Contains behavior contracts.

- `Persistable`
- `Matchable`

#### 7.4 `farmmarket.service`

Contains the business logic and file management.

- `MarketplaceManager`
- `FileManager`

#### 7.5 `farmmarket.util`

Contains supporting utility code.

- `AppConstants`
- `ValidationUtil`
- `ReportGenerator`

#### 7.6 `farmmarket.exceptions`

Contains custom exception classes.

- `InvalidDataException`
- `DuplicateIdException`
- `NoMatchFoundException`

#### 7.7 `farmmarket.ui`

Contains the desktop user interface.

- `MainFrame`
- `FarmerPanel`
- `BuyerPanel`
- `MatchPanel`
- `ReportsPanel`

#### 7.8 `farmmarket.web`

Contains the web server and API code.

- `MarketplaceWebServer`
- `ApiHandler`
- `StaticFileHandler`
- `HttpUtil`

---

### 8. Class-by-Class Explanation

This section is the heart of the report. It explains what each class contains and why it exists.

#### 8.1 `User`

File: [User.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/model/User.java)

This is the **abstract parent class** for all people in the system.

It contains private fields:

- `id`
- `name`
- `city`
- `phone`

It also provides:

- getters and setters
- built-in validation through setter methods
- abstract methods:
  - `getRole()`
  - `displayDetails()`
  - `toTableRow()`

Why it matters:

- avoids duplication
- demonstrates abstraction
- demonstrates inheritance
- demonstrates encapsulation

In simple words, `User` defines the common identity of a person in the system, while leaving specific behavior to subclasses.

#### 8.2 `Farmer`

File: [Farmer.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/model/Farmer.java)

This class extends `User` and implements `Matchable`.

Additional fields:

- `cropType`
- `quantityAvailable`
- `pricePerUnit`

Important methods:

- overloaded constructors
- `isCompatibleWith(Buyer buyer)`
- `calculateMatchScore(Buyer buyer)`
- overridden `displayDetails()`
- overridden `toString()`
- overridden `toCsv()`
- overridden `toTableRow()`

Why it matters:

- represents a real seller in the marketplace
- contains the logic for compatibility and scoring
- demonstrates inheritance, interface implementation, and overriding

This is one of the most important classes because it combines data and behavior in a clean OOP style.

#### 8.3 `Buyer`

File: [Buyer.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/model/Buyer.java)

This class also extends `User`.

Additional fields:

- `requiredCrop`
- `requiredQuantity`
- `maxBudget`

Important methods:

- overloaded constructors
- overridden `displayDetails()`
- overridden `toString()`
- overridden `toCsv()`
- overridden `toTableRow()`

Why it matters:

- represents demand in the marketplace
- stores the exact crop and budget requirements needed for matching

Together, `Farmer` and `Buyer` show how a parent class can be specialized in two different ways.

#### 8.4 `MatchRecord`

File: [MatchRecord.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/model/MatchRecord.java)

This class stores the result of a successful farmer-buyer comparison.

Fields include:

- buyer ID and buyer name
- farmer ID and farmer name
- crop type
- city
- available quantity
- price per unit
- score
- status

Important features:

- implements `Persistable`
- implements `Comparable<MatchRecord>`
- contains `toTableRow()`
- contains `toCsv()`
- defines `compareTo()` for score-based sorting

Why it matters:

- separates match result data from raw farmer and buyer data
- allows sorting of matches in descending score order
- supports reporting and CSV export

#### 8.5 `MarketplaceBackup`

File: [MarketplaceBackup.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/model/MarketplaceBackup.java)

This class stores:

- all farmers
- all buyers
- all last-generated matches

It implements `Serializable`.

Why it matters:

- allows backup using object streams
- demonstrates object-level persistence
- keeps backup logic clean and separate from the manager

#### 8.6 `Persistable`

File: [Persistable.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/interfaces/Persistable.java)

This interface contains one method:

- `toCsv()`

Why it matters:

- ensures objects that can be saved know how to convert themselves into CSV format
- demonstrates interface-based design

#### 8.7 `Matchable`

File: [Matchable.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/interfaces/Matchable.java)

This interface contains:

- `isCompatibleWith(Buyer buyer)`
- `calculateMatchScore(Buyer buyer)`

Why it matters:

- defines the behavior required for any class that can participate in matching
- is implemented by `Farmer`

This is a nice example of behavior abstraction.

#### 8.8 `MarketplaceManager`

File: [MarketplaceManager.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/service/MarketplaceManager.java)

This is the **central business logic class**.

It stores:

- list of farmers
- list of buyers
- list of last generated matches

It performs:

- adding farmers
- adding buyers
- searching farmers
- searching buyers
- generating matches
- generating all matches
- loading and saving through `FileManager`
- exporting and importing backup
- generating reports
- duplicate ID checking

Why it matters:

- it keeps business logic away from the UI
- it acts like the brain of the application
- it demonstrates loops, conditions, collections, exceptions, and overloading

If someone asks for the most important class in the project, `MarketplaceManager` is a very strong answer.

#### 8.9 `FileManager`

File: [FileManager.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/service/FileManager.java)

This class handles persistence.

Responsibilities:

- save farmers to CSV
- save buyers to CSV
- save matches to CSV
- load farmers from CSV
- load buyers from CSV
- export serialized backup
- import serialized backup
- create data folder if it does not exist

Key technical points:

- uses `BufferedWriter` and `FileWriter`
- uses `BufferedReader` and `FileReader`
- uses `ObjectOutputStream` and `ObjectInputStream`
- uses `try-with-resources`
- throws custom exceptions for invalid records

Why it matters:

- demonstrates file handling in a clean and practical way
- separates persistence from UI and business logic

#### 8.10 `AppConstants`

File: [AppConstants.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/util/AppConstants.java)

Contains:

- crop array
- file names
- data directory constants

Why it matters:

- demonstrates arrays
- avoids hardcoding repeated values

#### 8.11 `ValidationUtil`

File: [ValidationUtil.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/util/ValidationUtil.java)

Contains reusable validation methods such as:

- `requireNonEmpty`
- `requireNonNegativeDouble`
- `requireNonNegativeInt`
- `requirePositiveInt`
- `normalizeText`
- `matchesIgnoreCase`

Why it matters:

- keeps validation logic reusable and centralized
- improves code readability
- demonstrates custom exception usage

#### 8.12 `ReportGenerator`

File: [ReportGenerator.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/util/ReportGenerator.java)

Generates:

- summary report
- match report

Why it matters:

- keeps reporting logic separate from the UI
- shows string building and formatted text generation

#### 8.13 Exception Classes

Files:

- [InvalidDataException.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/exceptions/InvalidDataException.java)
- [DuplicateIdException.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/exceptions/DuplicateIdException.java)
- [NoMatchFoundException.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/exceptions/NoMatchFoundException.java)

These classes are simple but important.

They help the program communicate specific problems:

- invalid data entered by user or file
- duplicate IDs
- no compatible matches found

Why it matters:

- makes error handling more professional and readable
- shows custom exception creation

#### 8.14 Swing UI Classes

##### `MainFrame`

File: [MainFrame.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/ui/MainFrame.java)

Acts as the main window.

It creates:

- `MarketplaceManager`
- `FileManager`
- `FarmerPanel`
- `BuyerPanel`
- `MatchPanel`
- `ReportsPanel`

It also:

- loads initial data
- refreshes all tabs
- shows status messages

##### `FarmerPanel`

File: [FarmerPanel.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/ui/FarmerPanel.java)

Contains:

- input form for farmer data
- add/reset buttons
- search controls
- table display using `Vector` and `DefaultTableModel`
- validation and error dialogs

##### `BuyerPanel`

File: [BuyerPanel.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/ui/BuyerPanel.java)

Contains:

- input form for buyer data
- add/reset buttons
- search controls
- buyer table
- exception-based validation

##### `MatchPanel`

File: [MatchPanel.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/ui/MatchPanel.java)

Contains:

- buyer selection combo box
- match selected buyer button
- generate all matches button
- export matches button
- match table
- report text area

This panel is especially useful during demonstration because it clearly shows the matching logic at work.

##### `ReportsPanel`

File: [ReportsPanel.java](C:/Users/KrutarthPC/Desktop/OOP%20Project/src/farmmarket/ui/ReportsPanel.java)

Contains:

- save button
- load button
- backup button
- restore button
- summary report area

This class is important for showing file handling and object stream backup features.

#### 8.15 Web Classes

The project also contains a web version so it can be opened in browsers on laptops and phones.

##### `MarketplaceWebServer`

Starts the HTTP server and binds to `0.0.0.0`.

##### `ApiHandler`

Handles:

- farmer APIs
- buyer APIs
- match APIs
- summary API
- save/load API

##### `StaticFileHandler`

Serves HTML, CSS, and JavaScript files.

##### `HttpUtil`

Contains helper methods for:

- JSON response writing
- body reading
- query parsing
- basic JSON escaping

These classes are not required for the original syllabus, but they add a very useful practical extension.

---

### 9. How the Matching Logic Works

The matching logic is kept simple so that it is easy to explain in class.

The method `isCompatibleWith(Buyer buyer)` in `Farmer` checks:

1. buyer must not be null
2. crop type must match
3. city must match
4. price per unit must be less than or equal to buyer budget
5. quantity available must be greater than zero

If all of these are true, the buyer and farmer are considered compatible.

Then `calculateMatchScore(Buyer buyer)` gives a score:

- base score starts at `80`
- quantity closeness contributes up to `20`
- final score is rounded to 2 decimal places

This creates a ranked recommendation system without unnecessary complexity.

---

### 10. How Data Moves Through the System

The internal data flow of the project is:

1. UI accepts input
2. Input becomes an object
3. Object is validated
4. Manager stores object
5. Matching methods compare objects
6. Result becomes `MatchRecord`
7. Table and report show the result
8. File manager saves data

This separation is important because it makes the code organized:

- UI handles presentation
- manager handles logic
- models hold data
- file manager handles storage
- utilities support the system

---

### 11. Syllabus Coverage Explanation

This project covers the Java syllabus in a very natural way.

#### 11.1 Classes and Objects

Covered through all model, manager, and UI classes.

Examples:

- `Farmer farmer = new Farmer(...)`
- `Buyer buyer = new Buyer(...)`
- `MatchRecord match = new MatchRecord(...)`

#### 11.2 Arrays

Covered through `AppConstants.CROP_TYPES`.

#### 11.3 Strings

Used in IDs, crop names, city names, reports, file parsing, and UI labels.

#### 11.4 Vectors

Used in Swing tables for rows and columns.

#### 11.5 Operators, Loops, and Decision Making

Used in:

- searching
- file reading
- matching
- validation
- scoring

#### 11.6 Encapsulation

Private fields plus getters/setters in model classes.

#### 11.7 Constructors

Default and overloaded constructors in `Farmer` and `Buyer`.

#### 11.8 Inheritance

`Farmer` and `Buyer` inherit from `User`.

#### 11.9 Polymorphism

Overloading:

- constructors
- search methods

Overriding:

- `displayDetails()`
- `toString()`
- `toCsv()`
- `toTableRow()`

#### 11.10 Abstraction

Abstract class `User`.

#### 11.11 Interfaces

`Persistable` and `Matchable`.

#### 11.12 UML Diagrams

Included in the `docs/uml` folder.

#### 11.13 File Handling and IO Streams

CSV read/write plus serialized backup.

#### 11.14 Exception Handling

`try-catch`, `try-with-resources`, and custom exceptions.

---

### 12. UML Support in the Project

The project includes the following UML diagrams:

- use case diagram
- class diagram
- sequence diagram
- activity diagram

These help explain the project before even running the code.

The class diagram is especially valuable because it shows:

- abstract class
- interfaces
- inheritance
- association between manager and model classes
- custom exceptions

---

### 13. User Interface Design

The project contains two interfaces:

#### 13.1 Desktop interface

Built using Swing.

Advantages:

- simple to demonstrate in lab
- directly covers Java desktop UI concepts
- visually separates farmer, buyer, matching, and reports

#### 13.2 Web interface

Built using Java HTTP server with HTML, CSS, and JavaScript.

Advantages:

- can be opened on laptops and phones
- easier to share by link
- improves practical usefulness

---

### 14. Test Scenarios Covered

The system supports testing of:

- valid farmer creation
- valid buyer creation
- invalid blank fields
- negative quantity or price
- duplicate IDs
- file save and load
- match generation with valid compatibility
- no-match situations
- backup creation and restoration

This makes the project not only functional, but also easy to evaluate systematically.

---

### 15. Strengths of the Project

- clearly covers syllabus topics
- easy to explain during viva
- realistic real-world problem
- good package structure
- meaningful use of OOP
- file handling is practical, not artificial
- includes both desktop and web versions
- contains UML and documentation

---

### 16. Limitations

No academic project is perfect, and it is helpful to state limitations honestly.

Current limitations include:

- no database
- no login system
- no real distance calculation
- no order confirmation workflow
- no payment gateway
- no advanced analytics

These limitations are acceptable because the purpose of the project is educational clarity, not commercial deployment.

---

### 17. Future Enhancements

In future, the project can be extended with:

- user login and registration
- district or pincode-based matching
- real map distance integration
- admin dashboard
- database connectivity using MySQL
- order booking and status tracking
- online negotiation system
- analytics and demand forecasting

---

### 18. Conclusion

The **Farmer-Buyer Marketplace System** is a complete and meaningful Java project that successfully connects classroom concepts with a practical application.

Instead of showing Java topics in isolation, the project uses them in a natural workflow:

- classes represent real entities
- inheritance reduces duplication
- interfaces define reusable behavior
- custom exceptions improve reliability
- file handling preserves data
- GUI and web interface make the system interactive

Most importantly, this project is not just technically valid, but also **humanly understandable**. A teacher, examiner, or classmate can follow the flow without difficulty. That makes it especially suitable for an undergraduate report, demonstration, and viva.

---

### 19. Short Viva-Friendly Summary

If the project needs to be explained in one paragraph:

> This project is a Java-based Farmer-Buyer Marketplace System that stores farmers and buyers, compares crop type, price, and city, and recommends the best matches. It demonstrates classes, objects, arrays, strings, vectors, constructors, inheritance, polymorphism, abstraction, interfaces, file handling, custom exceptions, and UML diagrams. The project uses a clean package structure, supports both desktop and browser interfaces, and saves data using CSV files and serialized backup.
