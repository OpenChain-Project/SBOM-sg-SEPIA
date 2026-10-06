<!-- SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: CC-BY-4.0 -->
# Installation Guide

This guide will help you set up both the SBOM Public UI (frontend) and the SbomPublic Backend Service. Please follow the steps below.

---

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java 17**
- **Apache Maven 3.8.6**
- **Spring Boot 2.7.18**
- **Eclipse IDE** (or any preferred Java IDE)
- **Node.js** (version 14.x or higher recommended) - [Download Node.js](https://nodejs.org/)
- **npm** (comes with Node.js) - [Learn more](https://www.npmjs.com/)
- **Angular CLI** (recommended)  
  Install globally using:
  ```bash
  npm install -g @angular/cli
  ```
- **Visual Studio Code** (or any preferred code editor)
- **Operating System:** Windows 10/11 (win32 x64)

---

## 1. Setting Up SBOM Public UI (Frontend)

Follow these steps to set up and run the frontend application:

1. **Clone the repository:**
   ```bash
   git clone https://<repo-url>
   ```

2. **Install dependencies:**
  Open the downloaded source code(sbom-public-ui) using Visual Studio Code and then install the dependencies using
   ```bash
   npm install
   ```

3. **Run the development server:**
   ```bash
   ng serve
   ```
   The app will be available at [http://localhost:4200/](http://localhost:4200/) by default.

4. **For production build:**
   ```bash
   ng build --prod
   ```
   After the build completion deployable build source available inside dist directory.Move the build to your server
---

## 2. Setting Up SbomPublic Backend Service

Follow these steps to set up and run the backend service:

1. **Clone the repository:**
   ```bash
   git clone https://<repo-url>
   ```

2. **Build the project using Maven:**
  Open the downloaded source code(sbom-public-service) using Eclipse IDE(or any preferred Java IDE) and then install the dependencies using
   ```bash
   mvn clean install
   ```
   Inside the application.properties change the following properties
   sbom.upload.path=<Local folder path for saving uploaded BOM files>(Eg:c:\\temp\\sbom\\)

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
4. **For production build:**   
   to run the packaged JAR file:
   ```bash
   java -jar target/sbom-public-*.jar
   ```
   After the build completion deployable build source available inside target directory. Move the build to your server

---

## 3. Running the Application with Docker

The application is packaged as two Docker services: a backend API and a public UI. Both services are defined in `docker-compose.yaml` and can be started together using Docker Compose.

### Services

| Service           | Container                          | Host Port | Container Port | Description                |
|-------------------|--------------------------------|-----------|----------------|----------------------------|
| sbom-backend      | sbom-backend        | 9051      | 9051           | Backend API service        |
| sbom-public-ui    | sbom-frontend      | 4200      | 4200             | Public-facing web UI       |

### docker-compose.yaml

```yaml
services:
  backend:
    build: ./tools/sbom-public-service
    container_name: sbom-backend
    ports:
      - "9051:9051"   # Adjust as needed
    volumes:
      - ./data/sbom:/data/sbom

  frontend:
    build: ./tools/sbom-public-ui
    container_name: sbom-frontend
    ports:
      - "4200:4200"  # Adjust as needed
    depends_on:
      - backend
```

### Prerequisites

- Docker Engine installed and running
- Docker Compose v2 (`docker compose` CLI)

### Steps to Run

1. Clone the repository from github and navigate to the directory containing `docker-compose.yaml`:

   ```bash
   Example :
       cd C:\Users\SBOM-sg-SEPIA
   ```

2. Start the services:

   ```bash
   docker compose up --pull always
   ```

3. Verify the running containers:

   ```bash
   docker compose ps
   ```

### Access URLs

| Component        | URL                       |
|------------------|---------------------------|
| Public UI        | http://localhost:4200/#/     |
| Backend API      | http://localhost:9051/health     |

### Stopping the Application

To stop and remove the containers:

```bash
docker compose down
```
---

You are now ready to use both the frontend and backend services. If you encounter any issues, please refer to the project documentation or contact the maintainers.