# Harvest Hub Marketplace

Harvest Hub is a Java marketplace system for growers and buyers with:

- secure signup and login
- unique user IDs
- session-based authentication
- an embedded SQLite database
- a centralized dashboard
- dedicated pages for adding crops, buying crops, and profile management
- modern browser routing and a redesigned agriculture-inspired UI

## What Changed

- Replaced the previous file-only web flow with a database-backed web application.
- Added account creation, login, logout, and persistent sessions with HTTP-only cookies.
- Added a dashboard that shows user profile details, recent activity, marketplace totals, and top matches.
- Added ownership-aware crop listings and purchase requests, so each account can see its own history.
- Preserved the original sample marketplace data by importing the CSV records into the database on first run.

## Project Structure

- `src/farmmarket/service/MarketplaceDatabase.java` - SQLite-backed data and auth layer
- `src/farmmarket/web/ApiHandler.java` - authenticated web API
- `web/` - routed single-page frontend
- `data/` - legacy CSV seed data and runtime database location
- `lib/` - bundled JDBC and logging jars used by the web app

## How To Run

### PowerShell launcher

```powershell
.\run.ps1 -Mode web -OpenBrowser
```

### Manual compile

```powershell
$javac = ".tools\jdk\jdk-17.0.18+8\bin\javac.exe"
$java = ".tools\jdk\jdk-17.0.18+8\bin\java.exe"
$sourceFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path out | Out-Null
& $javac --add-modules jdk.httpserver -d out $sourceFiles
& $java --add-modules jdk.httpserver -cp "out;lib/*" farmmarket.web.MarketplaceWebServer 8080
```

Then open `http://localhost:8080`.

## Default Web Workflow

1. Create an account from the signup page.
2. Log in and land on the home dashboard.
3. Use the quick navigation cards to open:
   - Add Crops
   - Buy Crops
   - Profile
4. Track your posted crops, purchase requests, and suggested matches from the dashboard.

## Deployment Notes

- The Docker deployment now includes the `lib/` directory for SQLite support.
- Runtime data is stored in `data/marketplace.db`.
- The database file is ignored by Git, so each fresh deploy starts from the committed seed CSV data and then creates its own database.

## Legacy Desktop App

The original Swing desktop classes are still present and compile, but the main web experience is now the upgraded authenticated dashboard application.
