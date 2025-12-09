# 🚀 Space Invader Project

## 🧭 Overview
This project is a space shooting game developed using **Java** (and **Python** for AI mode).  
The development follows the **Gitflow workflow** and applies **Agile methodology** using **Scrum** and **Kanban**.  
Sprint progress and task tracking are managed through **Jira**.

---

## ⚙️ Development Workflow

- **Version Control:** Gitflow branching strategy
    - `master` – stable releases
    - `develop` – integration branch
    - `feature/*` – individual feature branches

- **Agile Process:** Scrum + Kanban
    - Sprint-based development
    - Task tracking and progress sharing via **Jira**
    - Continuous review and iteration

---

## 🧩 Features & Development Plan

### 1. Single / Multi Mode Selection
- Implement mode selection screen
- Separate **1P Mode** and **2P Mode**

---

### 2. Enemy HP System
- Change color based on HP for regular enemies
- Display HP bar for bosses
- Adjust **bomb collision logic** to prevent overlapping
- Fix **bullet hit detection** logic

---

### 3. Shop Stage Update
- Fix bug where no items appear in stage 1
- Implement **sequential shop upgrades** by stage
- Reset shop stage and money on death, but keep **skin points**

---

### 4. Simple Story Feature
- Display story window at the start of each stage
- Allow skipping with **Spacebar**

---

### 5. Login System
- Create login window
- Save and load user account information
- Link scores and achievements to User ID
  - HighScoreScreen displays only 1P Mode scores

---

### 6. Spaceship Skins
- Add **skin purchase** and **selection** interface
- Create skin assets
- Implement **skin points** (earned as total money / 10)

---

### 7. Infinite Mode
- Enemy HP increases over time
- Add **Infinite mode selection screen**
- Introduce **varied or randomized formations**
- Spawn **bosses periodically**
- Implement **infinite mode record system**
- Implement functions to **open and close the shop on demand**

---

### 8. AI Mode
> **Highest priority feature**

#### 🧠 API Specification (to be defined first)
- **Action:** move up, down, left, right, shoot
- **State:**
    - Frame
    - AI ship (x, y, cooldown)
    - bullets (x, y, id)
    - Enemy ships (x, y, hp, type)
    - Items (x, y, type)
    - Boss (x, y, hp, max hp)
    - Score
    - Enemy Damage Events (enemy id, damage)

#### ☕ Java Side
- Implement **HTTP communication module**
- Connect API calls within the **core game loop**
- Add **AI Mode** selection button

#### 🐍 Python Side
- Implement **Reinforcement Learning algorithm** (`agent.py`)
    - `agent.py` implements the core RL agent (DQN-based)
- Central control handled by **integrated server interface** (`ai_controller.py`)
    - Handles:
        - HTTP communication with Java game
        - State preprocessing & reward calculation
        - Action selection & model training

---

### 9. Balance Adjustment (optional)
- Tune enemy stats, item effects, and difficulty scaling

---

## ▶️ How to Run

### 1️⃣ Run the Java Game

```bash
./gradlew run
```

### 2️⃣️ Run the AI Client (Docker)
_No Python installation is required._
> **Note:** Run the following commands in a terminal opened inside the `TEAMKAWK` directory.

**Build the docker image:**
```bash 
docker build -t teamkwak-rl-ai ./rl
```

**Run the AI client:**
```bash 
docker run --rm --network="host" teamkwak-rl-ai
```

### 3️⃣ Launch the Game at Once (Recommened)
- Double-click **run_game_with_docker_ai.bat** file to start both the game and the AI client

---

## 🧱 Tech Stack

| Category | Technology |
|-----------|-------------|
| **Language** | Java, Python |
| **Version Control** | Git + Gitflow |
| **Project Management** | Jira (Scrum & Kanban) |
| **AI Integration** | REST API communication (Java ↔ Python) |

---

## 🪐 Notes
- The project will evolve through continuous iteration and review during each sprint.
- Tasks are tracked in **Jira**, with Git branches reflecting feature progress.
- Collaboration and pull requests follow the **Gitflow** structure for clarity and stability.

