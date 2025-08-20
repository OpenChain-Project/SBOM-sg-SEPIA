# Installation Guide

This guide will help you set up both the SBOM Public UI (frontend) and the SbomPublic Backend Service. Please follow the steps below.

---

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java 1.8**
- **Apache Maven 3.8.6**
- **Spring Boot 2.3.8**
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

You are now ready to use both the frontend and backend services. If you encounter any issues, please refer to the project documentation or contact the maintainers.