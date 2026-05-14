<div align="center">

```
███████╗███████╗██████╗ ██████╗ ██╗████████╗    ███████╗██╗      ██████╗ ██╗    ██╗
██╔════╝██╔════╝██╔══██╗██╔══██╗██║╚══██╔══╝    ██╔════╝██║     ██╔═══██╗██║    ██║
█████╗  ███████╗██████╔╝██████╔╝██║   ██║       █████╗  ██║     ██║   ██║██║ █╗ ██║
██╔══╝  ╚════██║██╔═══╝ ██╔══██╗██║   ██║       ██╔══╝  ██║     ██║   ██║██║███╗██║
███████╗███████║██║     ██║  ██║██║   ██║       ██║     ███████╗╚██████╔╝╚███╔███╔╝
╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝   ╚═╝       ╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝
```

### 🎓 The All-in-One Smart Academic Experience Platform

<br/>

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![ESPRIT](https://img.shields.io/badge/Made%20at-ESPRIT-E63946?style=for-the-badge)](https://esprit.tn/)

<br/>

> **Esprit Flow** is a full-featured desktop application built with Java & JavaFX that reimagines the academic experience — combining task management, AI coaching, health tracking, smart course recommendations, and real-time notifications into one sleek, intelligent platform.

<br/>

[✨ Features](#-features) · [🏗️ Architecture](#%EF%B8%8F-architecture) · [🔌 Integrations](#-api-integrations) · [⚙️ Setup](#%EF%B8%8F-installation--setup) · [📸 Screenshots](#-screenshots)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#%EF%B8%8F-architecture)
- [API Integrations](#-api-integrations)
- [Installation & Setup](#%EF%B8%8F-installation--setup)
- [Environment Variables](#-environment-variables)
- [Project Structure](#-project-structure)
- [Contributors](#-contributors)

---

## 🌟 Overview

**Esprit Flow** is built for students and administrators at ESPRIT school. It brings together everything needed for a productive academic life — from scheduling and task tracking to AI-powered health coaching and smart course discovery — all within a single, polished desktop interface.

The platform serves two distinct user roles:

| 👩‍💼 Administrator | 🎓 Student |
|---|---|
| Manage users, classes, subjects & rooms | Personal dashboard with tasks & calendar |
| Schedule sessions & assign tasks | Join/create groups & plan meetings |
| Monitor subscriptions & payments | AI health coaching & wellness tracking |
| View analytics & generate reports | Smart course discovery & streaks |
| Send alerts & manage groups | Premium subscription management |

---

## ✨ Features

### 🔐 Authentication & Security

- **Secure Login & Registration** — BCrypt password hashing, role-based access control (Admin / Teacher / Student)
- **Google OAuth 2.0** — One-click sign-in via Google account
- **Password Reset** — Token-secured reset flow with email delivery via Brevo
- **Face Recognition** — Identity verification using facial comparison
- **Ban Management** — Automated ban/unban email notifications

---

### 🤖 AI & Smart Features

<table>
<tr>
<td width="50%">

**🧠 AI Coaching (`CoachingPriveService`)**
> Powered by **Groq (LLaMA 3)** — generates personalized advice on sport, nutrition, and study organization based on your goals and progress.

</td>
<td width="50%">

**📊 Behavior Analysis (`BehaviorAnalysisService`)**
> Analyzes task patterns (average duration, deadline compliance, completion rate) and generates a detailed AI report with actionable recommendations.

</td>
</tr>
<tr>
<td>

**✅ Task Success Prediction (`TaskPredictionService`)**
> A local **Flask ML server** returns the probability of successfully completing a task based on historical patterns.

</td>
<td>

**🌤️ Weather AI (`WeatherAiService`)**
> Combines real-time weather from **OpenWeatherMap** with a Groq LLM to suggest outfit choices and an optimized study schedule for the day.

</td>
</tr>
</table>

---

### 📚 Smart Assistant — Course Discovery

> Find, enroll in, and track online courses with intelligent recommendations.

- 🔍 **Udemy Search** — Browse thousands of courses via keyword (powered by RapidAPI)
- 🎯 **Personalized Recommendations** — `RecommendationService` suggests courses based on your learning style profile (`StudentIntelligenceProfileService`)
- 🔥 **Streaks & Achievements** — Stay motivated with daily streaks and unlockable trophies
- 🛡️ **Rate Limiting** — `RateLimitService` ensures stable API usage

---

### 💬 Chatbot

> Ask anything — get smart, context-aware academic support in natural language.

- Powered by **GPT-4** via RapidAPI (`AcademicChatService`)
- Conversational interface built directly into the student dashboard

---

### 🏃 Health & Wellness Module

> Your complete personal wellness companion.

| Feature | Description |
|---|---|
| 🎯 **Personal Goals** | Set targets for hydration, sleep, sport & nutrition |
| 📈 **Progress Tracking** | Visual charts + automated PDF reports (iText) |
| 🥗 **Recipe Suggestions** | Healthy recipes from **Spoonacular** by calories, keywords & prep time |
| 💪 **Workout Plans** | Exercises filtered by muscle, equipment & intensity via **WorkoutAPI** |
| 🤖 **AI Health Coach** | Groq-powered personalized daily coaching |

---

### 💳 Subscriptions & Payments

> Seamless premium plan management powered by Stripe.

1. Student selects a subscription pack (Free / Premium)
2. **Stripe Checkout Session** is created server-side
3. User completes secure payment on Stripe's hosted page
4. App records subscription & expiry — auto-renew reminders sent via email

---

### 🔔 Notifications & Communication

- **In-app notifications** — Real-time alerts for tasks, meetings & group activity
- **Email notifications** — Task reminders, deadline warnings, invitations & PDF attachments via **Brevo**
- **Scheduled reminders** — Smart scheduling based on user alert preferences

---

### 👥 Groups & Collaboration

- Create or join academic groups
- Publish posts & comments with notification triggers
- Propose and schedule group meetings
- Quiz module — create, share & evaluate questionnaires

---

## 🏗️ Architecture

Esprit Flow follows a clean **MVC / MVVM** pattern:

```
src/main/java/
├── 🗂️ entities/          → Domain models (User, Task, Group, Objective…)
├── ⚙️ services/           → Business logic, CRUD operations, API calls
│   ├── ai/               → Groq, WeatherAI, ChatService
│   ├── health/           → Coaching, Recipes, Workouts
│   ├── payments/         → Stripe integration
│   ├── notifications/    → Brevo email service
│   └── courses/          → Udemy, Streaks, Achievements
├── 🖥️ controllers/        → JavaFX controllers bound to FXML views
├── 🔌 api/               → External API wrappers
└── 🛠️ utils/             → Helpers (crypto, validation, PDF…)

src/main/resources/
├── 🎨 views/             → FXML layout files
├── 💅 styles/            → CSS stylesheets
└── 🖼️ images/            → Application assets
```

### Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21 + FXML |
| Build Tool | Apache Maven |
| Database | MySQL 8 (via JDBC) |
| ML Server | Python Flask (local) |
| PDF Generation | iText |
| Password Hashing | BCrypt |

---

## 🔌 API Integrations

| Service | Purpose | Endpoint / Notes |
|---|---|---|
| ![Groq](https://img.shields.io/badge/Groq-LLaMA_3-F55036?style=flat-square) | AI coaching, behavior analysis, weather advice | `POST https://api.groq.com/openai/v1/chat/completions` |
| ![OpenWeatherMap](https://img.shields.io/badge/OpenWeatherMap-Weather-EB6E4B?style=flat-square) | Real-time weather for daily planning | `api.openweathermap.org/data/2.5/weather` |
| ![Spoonacular](https://img.shields.io/badge/Spoonacular-Recipes-43A047?style=flat-square) | Healthy recipe search by keyword, calories & time | REST API |
| ![WorkoutAPI](https://img.shields.io/badge/WorkoutAPI-Exercises-1565C0?style=flat-square) | Workouts filtered by muscle, equipment & intensity | REST API |
| ![Stripe](https://img.shields.io/badge/Stripe-Payments-635BFF?style=flat-square) | Secure subscription checkout sessions | Stripe Checkout |
| ![Brevo](https://img.shields.io/badge/Brevo-Email-0092FF?style=flat-square) | Transactional emails, reminders, PDF attachments | SMTP + API |
| ![RapidAPI](https://img.shields.io/badge/RapidAPI-ChatGPT_4-2D96FF?style=flat-square) | Student chatbot (GPT-4 via proxy) | RapidAPI Hub |
| ![Google](https://img.shields.io/badge/Google-OAuth_2.0-4285F4?style=flat-square) | One-click Google sign-in | OAuth 2.0 |
| ![Udemy](https://img.shields.io/badge/Udemy-Courses-A435F0?style=flat-square) | Online course search for Smart Assistant | via RapidAPI |
| ![Flask](https://img.shields.io/badge/Flask-ML_Model-000000?style=flat-square) | Task success probability prediction | Local endpoint |

---

## ⚙️ Installation & Setup

### Prerequisites

Make sure you have the following installed:

- ☕ **Java 17+** — [Download here](https://adoptium.net/)
- 🎨 **JavaFX 21** — [Download here](https://openjfx.io/)
- 🗄️ **MySQL 8.0+** — [Download here](https://dev.mysql.com/downloads/)
- 📦 **Apache Maven 3.x** — [Download here](https://maven.apache.org/)
- 🐍 **Python 3.x + Flask** *(for ML prediction server)*

### Step-by-Step

**1. Clone the repository**
```bash
git clone https://github.com/your-username/esprit-flow.git
cd esprit-flow
```

**2. Set up the database**
```sql
CREATE DATABASE espritflow;
-- Then import the provided SQL scripts:
mysql -u root -p espritflow < database/schema.sql
```

**3. Start the ML prediction server**
```bash
cd ml-server
pip install -r requirements.txt
python app.py
```

**4. Create your `.env` file** at the project root (see section below)

**5. Build and run**
```bash
mvn clean install
mvn javafx:run
```

---

## 🔑 Environment Variables

Create a `.env` file at the project root with the following:

```env
# ── Database ──────────────────────────────────────────
DB_URL=jdbc:mysql://localhost:3306/espritflow
DB_USER=your_mysql_user
DB_PASSWORD=your_mysql_password

# ── AI / Groq ─────────────────────────────────────────
GROQ_API_KEY=your_groq_api_key
GROQ_MODEL=llama-3-8b

# ── Weather ───────────────────────────────────────────
OPENWEATHER_API_KEY=your_openweathermap_key
OPENWEATHER_CITY=Tunis

# ── Recipes & Workouts ────────────────────────────────
SPOONACULAR_API_KEY=your_spoonacular_key
WORKOUT_API_KEY=your_workout_api_key

# ── Email (Brevo) ─────────────────────────────────────
BREVO_API_KEY=your_brevo_key
BREVO_SENDER=noreply@yourdomain.com

# ── Chatbot & Courses (RapidAPI) ──────────────────────
RAPIDAPI_KEY=your_rapidapi_key
UDEMY_API_KEY=your_udemy_key

# ── Payments (Stripe) ─────────────────────────────────
STRIPE_PUBLIC_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...

# ── Authentication (Google OAuth) ─────────────────────
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# ── Mail (SMTP fallback) ──────────────────────────────
MAIL_USER=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

> ⚠️ **Never commit your `.env` file to version control.** It is already listed in `.gitignore`.

---

## 📁 Project Structure

```
esprit-flow/
├── 📄 pom.xml                    → Maven configuration & dependencies
├── 🔐 .env                       → Environment variables (git-ignored)
├── 📂 src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controllers/      → JavaFX UI controllers
│   │   │   ├── entities/         → Data models
│   │   │   ├── services/         → Business logic & API calls
│   │   │   ├── api/              → External API clients
│   │   │   ├── utils/            → Utility classes
│   │   │   └── mains/            → Application entry points
│   │   └── resources/
│   │       ├── fxml/             → FXML view files
│   │       ├── css/              → Stylesheets
│   │       └── images/           → Assets & icons
├── 📂 ml-server/                 → Flask ML prediction server
│   ├── app.py
│   ├── model.pkl
│   └── requirements.txt
└── 📂 database/
    └── schema.sql                → Database schema
```

---

## 👥 Contributors

<div align="center">

| Made with ❤️ by the **Esprit Flow Team** at **ESPRIT School of Engineering** |
|:---:|

</div>

---

<div align="center">

**⭐ If you found this project useful, please consider giving it a star!**

<br/>

[![Stars](https://img.shields.io/github/stars/your-username/esprit-flow?style=social)](https://github.com/your-username/esprit-flow)

<br/>

*Built with Java · JavaFX · MySQL · AI · ☕*

</div>
