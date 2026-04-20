# 🚀 FarmToCloud - Project Run Instructions

Welcome to the **FarmToCloud** supply chain platform! This document provides a detailed, step-by-step guide to running the entire project (Backend & Frontend) seamlessly.

Since this project utilizes a Monolithic Architecture, the **Frontend (HTML/CSS/JS)** is neatly bundled with the **Backend (Spring Boot Java)**. This means you only need to run a single application on a single port to access everything.

---

## 📋 Prerequisites

Before running the application, please make sure you have the following installed on your machine:

1. **Java Development Kit (JDK 17 or higher)**
   - To verify, open your terminal (Command Prompt/PowerShell) and type: `java -version`
2. **Maven (Optional but recommended)**
   - To verify, type: `mvn -v`
3. **An IDE (Integrated Development Environment)**
   - Recommended: IntelliJ IDEA, Eclipse, or Visual Studio Code (with the Java Extension Pack).
4. **Active Internet Connection**
   - *Why?* The project is configured to use a cloud-hosted MongoDB Atlas database. You **do not** need to install MongoDB locally!

---

## ⚙️ Database Configuration (Pre-configured!)

You don't need to do any heavy lifting for the database!
The application is already configured to speak with a highly-available **MongoDB Atlas cloud cluster**.
You can see this configuration inside `src/main/resources/application-dev.yml`.

Because the database lives in the cloud, all you need is an internet connection when you hit "Run".

---

## ▶️ Step-by-Step Guide to Run the Application

You can run the project in two ways: Using your IDE (Easiest) or using the Command Line.

### Method 1: Using your IDE (IntelliJ IDEA / Eclipse) 🌟 Recommended

1. **Open your IDE.**
2. Select **Open** and browse to the primary project folder (`C:\Users\Prakash\OneDrive\Desktop\BMD`), which contains the `pom.xml` file.
3. Wait for the IDE to finish indexing and downloading Maven dependencies (you will usually see a progress bar at the bottom right).
4. Navigate through the project structure to find the main Application file:
   - Go to: `src/main/java/net/farmtocloud/app/FarmToCloudApplication.java`
5. Right-click inside `FarmToCloudApplication.java` and select **"Run 'FarmToCloudApplication.main()'"**.
6. Wait for the Spring Boot console to start up. You will know it is ready when you see a log similar to:
   > `Started FarmToCloudApplication in X.XXX seconds (JVM running for Y.YYY)`
   > `Tomcat initialized with port(s): 8080 (http)`

### Method 2: Using the Command Line / PowerShell

1. Open PowerShell or Command Prompt.
2. Navigate to the project root directory:
   ```cmd
   cd c:\Users\Prakash\OneDrive\Desktop\BMD
   ```
3. Run the application using the Maven wrapper or your global Maven installation:
   ```cmd
   mvn spring-boot:run
   ```
   *(Or if using the maven wrapper, `./mvnw spring-boot:run`)*
4. Wait until the console output finishes and stops at the `Started FarmToCloudApplication` log. Do not close this terminal!

---

## 🌐 Navigating the Frontend Application

Congratulations! The single monolithic server is now happily running the backend API and delivering the frontend pages. To interact with the project, open your favorite web browser (Google Chrome, Edge, Safari, etc.) and visit these links:

* 🏠 **Main Landing Page:** 
  [http://localhost:8080/](http://localhost:8080/)
  *This is the entry point displaying project features with Sign Up and Login workflows.*

* 🕹️ **Interactive Prototype Demo:**
  [http://localhost:8080/prototype.html](http://localhost:8080/prototype.html)
  *Use this file to give a fast, interactive demonstration without relying heavily on live database flows.*

* 🎬 **Auto-Generating Video Presenter:**
  [http://localhost:8080/demo-video.html](http://localhost:8080/demo-video.html)
  *Use this if you need to record a crisp MP4/WebM of your project presentation.*

### Order of Operations (Testing the Live API workflow)
If you want to test the full live end-to-end workflow on the `localhost:8080/` app:
1. Hit **Sign up** as a **FARMER** (Create credentials).
2. Look at the **Smart Price Engine** and list a crop for sale.
3. Open a second Browser Window (or Incognito Mode).
4. Hit **Sign up** as a **CLOUD KITCHEN** (Create credentials).
5. Purchase the crop the farmer listed.
6. Hop between the dashboards to push the order status from *Ordered* ➔ *Picked Up* ➔ *Verified* ➔ *Confirmed* ➔ *Delivered*.

Enjoy testing FarmToCloud! 🎉
