#  Premium Task Manager – Modern Dark UI Dashboard (Jetpack Compose)

Welcome to the **Task Manager** portfolio/resume project. It is a modern, high-fidelity productivity and workflow-tracking application designed with a polished dark theme layout, responsive Material 3 components, and robust local persistence.

This project showcases clean development principles (**MVVM architecture**, **Room Database SQLite integration**, **Secure Session Validation**, and **Jetpack Navigation**) in a beautiful single-view dashboard inspired by premium developer dashboards.

---

##  Visual Experience & Aesthetic Choices

This application moves entirely away from generic defaults, committing with restraint to a bold **Obsidian Synth Slate** identity:
- **Obsidian Dark Canvas (`#0F172A`)**: A deep Slate-900 baseline container that maximizes readability and eye-comfort, perfect for long-run productivity sessions.
- **Cyber Cyan Primary Accent (`#06B6D4`)**: High-contrast branding that isolates core actions (such as adding tasks) for quick navigation.
- **Vivid Rose Highlights (`#F43F5E`)**: Alerts users immediately of critical actions like deletions and alerts.
- **Micro-Animations & Ripple feedback**: Standard M3 ripples on all clickable components, using spring easing for high-fluidity feels.

---

##  Key Functional Modules

###  1. Protected JWT-Like Session Security
- **Registration Screen**: Captures and validates User Credentials (Name, Email, and min 6-character Passwords). Passwords are securely hashed using a built-in **SHA-256 MessageDigest** before saving to SQL tables.
- **Login Screen**: Authenticates against existing users in the local SQLite engine.
- **Session Management**: Simulates Token-based Auth (JWT standard) by managing unique session signatures inside local `SharedPreferences` to secure subsequent access.
- **Protected Routing**: The app checks standard login tokens on immediate startup and routes users safely to the Main Dashboard or Auth forms.

###  2. Interactive Analytical Dashboard
- **Completion Progress Ring**: A circular animated progress indicator showing the user’s exact workflow complete-ratio dynamically.
- **KPI Metrics Strip**: Beautiful counter cards for active **Pending** and **In Progress** items, updating reactively on change.

###  3. Responsive Task CRUD & Filter Controls
- **Flexible Filters**: Smooth horizontal-scrolling category chips allow filtering by context tags like: `Work`, `Personal`, `Health`, `Shopping`, and `Other`.
- **Workflow Tabs**: Instantly swap status streams between *Completed*, *In Progress*, or *Pending*.
- **Real-time Querying**: Local search bar filters title headings and descriptions with no main thread block.
- **Dialog Controls**: Highly structured modal inputs to create and modify task title details, due dates, statuses, and category descriptors.

---

##  Architecture & Folder Structure

We follow the strictly recommended clean development directory hierarchy:

```text
com.example/
│
├── data/                            # Core Data Layer
│   ├── model/
│   │   ├── User.kt                 # User Entity Class (id, name, email, passwordHash)
│   │   └── Task.kt                 # Task Entity Class (id, title, description, category, status, userId)
│   │
│   ├── local/
│   │   ├── UserDao.kt              # Handles SQL User authentication operations 
│   │   ├── TaskDao.kt              # Handles SQL Tasks CRUD queries
│   │   ├── AppDatabase.kt          # Main Room DB Instance (version 1)
│   │   └── SessionManager.kt       # Stores JWT representation in local SharedPreferences
│   │
│   └── repository/
│       ├── UserRepository.kt       # Intermediary for account setup & validation flow
│       └── TaskRepository.kt       # Intermediary for local tasks actions
│
├── viewmodel/
│   └── AppViewModel.kt             # Viewstate controller combining flow queries, stats, & actions
│
└── ui/                              # UI Presentation Layer
    ├── theme/
    │   ├── Color.kt                # Custom Cyber Slate palette (Obsidian, Cyan, Emerald)
    │   ├── Type.kt                 # Material typography style metrics
    │   └── Theme.kt                # Jetpack Compose Theme configuration (ObsidianDark default)
    │
    └── screens/
        ├── LoginScreen.kt          # UI forms and states for account logins
        ├── RegisterScreen.kt       # UI forms and states for account creations
        └── DashboardScreen.kt      # Main workflow grid, statistics charts, and CRUD interfaces
```

---

##  Tech Stack & Dependencies

The project is built on the following production-tested components managed in `libs.versions.toml`:
- **Kotlin 2.2.10** & **Jetpack Compose Native UI**
- **Room Persistence Database (2.7.0)**: Relational SQLite management using **KSP (Kotlin Symbol Processing)**.
- **Jetpack Navigation Composable (2.8.9)**: Type-safe screen navigation and backstack protection.
- **Material 3 Icons (Extended)**: Premium visual icons for diverse task categories.

---

##  API Expansion Guide (From Simulated Local to REST Full-Stack)

If you are presenting this project in a junior developer interview, you can easily explain how you can scale this offline-first client-side architecture to connect with your **Express.js / Node.js and MongoDB** full-stack:

1. **Authentication API:**
   - Swap the local `userRepository.registerUser` / `userRepository.loginUser` calls inside `AppViewModel` with raw **Retrofit SDK** POST requests to:
     - `POST /api/auth/register` (parameters: `name`, `email`, `password`)
     - `POST /api/auth/login` (parameters: `email`, `password`)
   - Save the returned `token` JWT string using `SessionManager.saveSession`.

2. **Task APIs:**
   - In `TaskRepository`, replace standard `TaskDao` interactions with Retrofit endpoint declarations:
     - `GET /api/tasks` -> Retrieve list and sync/persist with a single local SQLite Room table copy as cache.
     - `POST /api/tasks` -> Save new dynamic tasks.
     - `PUT /api/tasks/:id` -> Update specific status categories.
     - `DELETE /api/tasks/:id` -> Remove obsolete pipeline records.

---

##  Portability & Deployment

- **APK Compilation**: The app builds an optimized debug APK. It can be built locally using `gradle assembleDebug` or `compile_applet` inside the AI Studio container workspace.
- **Emulator Verification**: Visually accessible in real-time via the Streaming Android Emulator.
- **GitHub Ready**: Features clean modular file directories, no hardcoded API keys/passwords, and robust standard error-handling structures.
