# Farmer-Buyer Marketplace System

A Java OOP project with:

- a Swing desktop application
- a browser-based web application

The system connects farmers with buyers using:

- crop availability
- price compatibility
- same-city proximity

## Features

- Add and view farmers
- Add and view buyers
- Search farmers and buyers by crop and city
- Generate ranked matches for one buyer or all buyers
- Save and load CSV data using file handling and IO streams
- Export serialized backup using object streams
- Display summary reports in the GUI and browser
- Demonstrate exception handling with custom exceptions

## OOP Topics Covered

- Classes and objects
- Arrays, strings, vectors
- Operators, loops, decision making
- Encapsulation
- Constructors
- Inheritance
- Polymorphism through overloading and overriding
- Abstraction
- Interfaces
- UML diagrams
- File handling and IO streams
- Exception handling and custom exceptions

## Project Structure

- `src/farmmarket/app` - application entry point
- `src/farmmarket/model` - domain classes
- `src/farmmarket/interfaces` - interfaces
- `src/farmmarket/service` - manager and file handling
- `src/farmmarket/ui` - Swing GUI
- `src/farmmarket/web` - Java web server
- `src/farmmarket/util` - constants, validation, reporting
- `web` - browser frontend files
- `data` - sample CSV files
- `docs/uml` - UML source diagrams

## How To Run

### Easiest option

Run the PowerShell launcher:

```powershell
.\run.ps1 -Mode web -OpenBrowser
```

This starts the website version on `http://localhost:8080`.

To start the desktop Swing version instead:

```powershell
.\run.ps1 -Mode desktop
```

### Manual compile and run

1. Install JDK 17 or later.
2. Open a terminal in the project folder.
3. Compile:

```powershell
javac --add-modules jdk.httpserver -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

4. Run the desktop app:

```powershell
java -cp out farmmarket.app.FarmerBuyerMarketplaceApp
```

5. Run the web app:

```powershell
java --add-modules jdk.httpserver -cp out farmmarket.web.MarketplaceWebServer 8080
```

Then open `http://localhost:8080`.

## Sample Files

- `data/farmers.csv`
- `data/buyers.csv`
- `data/matches.csv`

The app loads farmer and buyer data from the `data` folder at startup if the files exist.

## Share As A Link

### Same Wi-Fi or local network

Run:

```powershell
.\run.ps1 -Mode web
```

The script prints a network URL such as `http://192.168.1.5:8080`.
Anyone on the same Wi-Fi can open that link from a phone or PC browser.

### Public internet sharing

This project includes a `Dockerfile` and `render.yaml`, so the simplest free deployment path is:

- push the project to GitHub
- create a free Render account
- create a Render Blueprint from the repo
- deploy and share the generated HTTPS link

The Java web server already binds to `0.0.0.0` and supports a platform-provided `PORT`, which is required for public web deployment.

For a full click-by-click guide, see `docs/deployment.md`.

### Important note about saved data

This project stores data in local CSV files inside the `data` folder.

That is fine for:

- demos
- class submissions
- portfolio links

But on free cloud hosting, added data may reset after redeploys, restarts, or instance recycling. Treat the public deployment as a showcase version unless you later add a database.

## Upload To GitHub Safely

When creating your GitHub repo, upload these folders and files:

- `src/`
- `web/`
- `data/`
- `docs/`
- `Dockerfile`
- `render.yaml`
- `README.md`
- `.dockerignore`
- `.gitignore`

Do not upload build/runtime folders such as:

- `.tools/`
- `out/`
- `*.log`

## Notes

- Matching is intentionally simple for classroom explanation.
- The project uses file storage only, not a database.
- No login, payment, or external database is included.
