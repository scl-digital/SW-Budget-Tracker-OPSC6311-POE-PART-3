👥 Group Members
Shinsa Lyonga Lomboto – Student No: [ ST10312918 ]
Khanya Mdyosi  – Student No: [ ST10376145 ]
Babalo Nogqala  – Student No: [ ST10268692 ]
Lwando Sizani  – Student No: [ ST10384260 ]
Wolathile Putu  – Student No: [ ST10354137 ]

---

💰 Personal Budget Tracker App 📊

Welcome to the **Personal Budget Tracker App** — a modern Android application built with Kotlin for tracking personal expenses, setting financial goals, and visualizing your financial health. The app is designed for simplicity, security, and effectiveness, helping users take control of their finances.

---

📖 Table of Contents
- [Purpose](#purpose)
- [Design Considerations](#design-considerations)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [GitHub & Collaboration](#github--collaboration)
- [GitHub Actions & CI/CD](#github-actions--cicd)
- [Demo Video](#demo-video)
- [APK Download](#apk-download)
- [How to Run](#how-to-run)
- [Adding Images to the README](#adding-images-to-the-readme)

---

📝 Purpose
The **Personal Budget Tracker App** aims to empower users to:
- Track income and expenses efficiently
- Set and monitor monthly financial goals
- Visualize spending patterns with charts and summaries
- Stay motivated with achievements and financial tips
- Collaborate on budgets with family or partners

The app is ideal for anyone seeking to improve their financial literacy and discipline, from students to working professionals.

---

🎨 Design Considerations
Our design philosophy focused on:
- **User-Centric Interface:** Clean, intuitive UI with clear navigation and prominent action buttons.
- **Offline-First:** Data is stored locally using RoomDB for reliability, with optional cloud sync via Firebase for backup and sharing.
- **Visual Feedback:** Real-time charts and summaries for actionable insights.
- **Security:** Secure authentication and data handling.
- **Responsiveness:** Optimized layouts for all Android devices.
- **Consistency:** Standardized navigation and theming for a cohesive experience.
- **Motivation:** Badges, achievements, and financial tips to encourage good habits.

---

🧠 Features
- 🔐 **Login & Registration:** Secure user authentication
- 📁 **Category Management:** Create/manage budget categories (Food, Transport, Rent, etc.)
- 🧾 **Expense Entry:** Add expenses with date, time, description, category, and optional photo
- 🎯 **Goal Setting:** Set min/max monthly spending goals
- 📅 **View Expenses:** Filter and view expenses by date range, with photo support
- 📊 **Category Summary:** See total spent per category
- 💾 **Data Persistence:** Offline storage with RoomDB (SQLite)
- 🏆 **Achievements & Badges:** Unlock and display milestones
- 💡 **Financial Tips:** Rotating tips on the home page
- 🔗 **Budget Sharing:** Collaborate via Firebase
- 🔄 **Cross-Platform Sync:** Sync data across devices

---

🛠 Technologies Used
- Kotlin
- Android SDK
- RoomDB (SQLite)
- Firebase (Auth, Database, Storage)
- MPAndroidChart (for analytics)

---

🧑‍💻 GitHub & Collaboration
This project is managed on GitHub, enabling:
- **Version Control:** Track changes, roll back, and manage releases
- **Collaboration:** Multiple contributors can work together via branches and pull requests
- **Issue Tracking:** Bugs and feature requests are managed through GitHub Issues
- **Documentation:** All documentation, including this README, is versioned and accessible

How we use GitHub:**
- Each feature or bugfix is developed in a separate branch
- Pull requests are used for code review and discussion
- Issues are created for bugs, enhancements, and tasks
- Releases are tagged for APK distribution

---

⚙️ GitHub Actions & CI/CD
**GitHub Actions** is a powerful tool for automating workflows such as building, testing, and deploying code. While this project does not currently have any GitHub Actions workflows set up, here's how they could be used:
- **Automated Builds:** Compile the app on every push or pull request
- **Automated Testing:** Run unit and UI tests automatically
- **Release Automation:** Build and upload APKs to GitHub Releases
- **Code Quality:** Lint and static analysis on every commit

To set up GitHub Actions, create a `.github/workflows` directory and add YAML workflow files. Example:*
```yaml
name: Android CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
```

---

📹 Demo Video
▶️ [Watch the demo video here](https://www.youtube.com/watch?v=1X50gWf8iuc)

---

📦 APK Download
Click [here](https://github.com/scl-digital/OPSC6311-Part2-Budget-Tracker/releases/tag/v1.0.0) to download the APK.

---

📂 How to Run

> **⚠️ Note:** This project requires **Java 17**. Please ensure you have JDK 17 installed and configured in your environment for compatibility.

1. Clone the repo:
   ```sh
   git clone https://github.com/scl-digital/OPSC6311-Part2-Budget-Tracker.git
   ```
2. Open in Android Studio
3. Build and run on an emulator or device

---

🖼️ Adding Images to the README
To add images, upload them to your repository (e.g., in a `docs/` or `images/` folder) and use Markdown:
```md
![Description](images/screenshot1.png)
```
You can also use external image links:
```md
![App Home Screen](https://your-image-url.com/home.png)
```

---

*Make this README your own! Add screenshots, diagrams, and badges to showcase your project.*
