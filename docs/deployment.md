# Deployment Guide

## Best free option for this project

The simplest free setup for this Java project is:

- GitHub for source code hosting
- Render Free Web Service for public deployment

This repo is already prepared for that path because it includes:

- `Dockerfile`
- `render.yaml`
- a Java web server that binds to `0.0.0.0`
- `PORT` environment support for cloud platforms

## Before you deploy

### What to upload to GitHub

Upload these items:

- `src/`
- `web/`
- `data/`
- `docs/`
- `Dockerfile`
- `render.yaml`
- `README.md`
- `.dockerignore`
- `.gitignore`

Do not upload:

- `.tools/`
- `out/`
- `*.log`

### Important limitation

The app stores farmers, buyers, and matches in local CSV files under `data/`.

That means the public Render deployment should be treated as a demo or showcase site. On free hosting, saved records may not remain forever after redeploys, restarts, or free-tier instance recycling.

## Step 1: Create a GitHub repository

1. Sign in to GitHub.
2. Create a new repository.
3. Give it a simple name such as `farmer-buyer-marketplace`.
4. Set it to `Public` if you want to share it easily.
5. Upload this project folder, excluding `.tools`, `out`, and log files.

## Step 2: Create a Render account

1. Sign in at Render.
2. Use the free plan.
3. Connect your GitHub account when asked.

## Step 3: Deploy with Render Blueprint

1. In Render, click `New`.
2. Choose `Blueprint`.
3. Select your GitHub repository.
4. Choose the branch you uploaded, usually `main`.
5. Render will detect `render.yaml`.
6. Confirm the generated web service settings.
7. Click `Apply` or `Deploy Blueprint`.

The included `render.yaml` already tells Render to:

- create a web service
- use Docker
- use the free plan
- expose the health check at `/api/health`
- set `PORT=10000`

## Step 4: Wait for the first deploy

Render will build the Docker image and then start the app.

The build uses the existing `Dockerfile`, which:

- copies `src/`, `web/`, and `data/`
- compiles the Java source
- starts `farmmarket.web.MarketplaceWebServer`

After deployment, Render will generate a public URL like:

```text
https://your-service-name.onrender.com
```

## Step 5: Test the deployed site

Open the main URL and verify:

- the homepage loads
- the summary counters load
- the tables appear correctly
- the forms submit correctly

Also open this health endpoint:

```text
https://your-service-name.onrender.com/api/health
```

Expected result:

```json
{"status":"ok"}
```

Then test these actions in the web app:

1. Add one farmer.
2. Add one buyer.
3. Search farmers or buyers.
4. Generate a match.
5. Click `Save Data`.
6. Click `Reload Data`.

## Step 6: Share and update

Once the site is live, share the public Render URL.

For future updates:

1. Edit the project locally.
2. Push the changes to the same GitHub repository.
3. Render will automatically redeploy.

## Local run option

If you want to test before deploying, run:

```powershell
.\run.ps1 -Mode web -OpenBrowser
```

Then open:

```text
http://localhost:8080
```

If someone is on the same Wi-Fi or local network, they can use the printed network URL.

## Why this deployment works

- `src/farmmarket/web/MarketplaceWebServer.java` binds to `0.0.0.0`
- `src/farmmarket/web/MarketplaceWebServer.java` reads the platform `PORT`
- `web/` contains the frontend
- `src/farmmarket/web/ApiHandler.java` serves the backend API
- `render.yaml` matches Render's Blueprint flow
