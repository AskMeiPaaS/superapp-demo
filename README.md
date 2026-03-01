📱 Adaptive UI SuperappA Hyper-Personalized, Server-Driven UI (SDUI) Architecture powered by Java 21, MongoDB, and Local AI.This project demonstrates a next-generation "Superapp" architecture where the user interface is not hardcoded on the client. Instead, it is dynamically generated and "morphed" by a Java 21 backend based on real-time user context (location, time, habits) and refined by a Small Language Model (Phi-3) running locally.🏗️ ArchitectureThe system is built on a decoupled, containerized microservices architecture:ServiceTechnologyRoleFrontendNode.js (Express + EJS)A lightweight "dumb" renderer. It captures user signals (GPS, Time) and renders whatever JSON layout the backend sends.BackendJava 21 (Spring Boot)The high-performance brain. Uses Virtual Threads (Project Loom) to handle massive concurrency while orchestrating data from DB and AI.DatabaseMongoDB 7.0Stores flexible UI component schemas ("Lego blocks") and user behavior vectors.AI EngineOllama (Phi-3)A local Small Language Model (SLM) that rewrites UI copy in real-time to match the user's specific intent.System Diagramgraph TD
    User[Mobile/Web User] -->|1. Context Signals| Frontend(Node.js Renderer)
    Frontend -->|2. Request Layout| Backend(Java 21 Service)
    Backend -->|3. Get Intent| AI(Ollama / Phi-3)
    Backend -->|4. Query Template| DB[(MongoDB)]
    Backend -->|5. Hydrate & Personalize| Backend
    Backend -->|6. JSON Schema| Frontend
    Frontend -->|7. Render Native UI| User
🚀 Message FlowSignal Capture: The user opens the app. The Frontend captures context (e.g., "User is at the gym, 7:00 PM").Intent Classification: The Frontend sends this context to the Backend. The Java service asks the AI Model: "What does a user at the gym want?" -> AI answers: "RECOVERY_FOOD".Template Retrieval: The Backend queries MongoDB for UI components tagged with #RECOVERY_FOOD.Dynamic Copywriting: The Backend asks the AI again: "Write a button label for a protein shake promo for a tired gym user." -> AI generates: "Refuel with 20% Off".Response: The Backend bundles this into a JSON SDUI payload.Rendering: The Frontend maps the JSON to native widgets (Buttons, Cards) and displays them.🛠️ PrerequisitesDocker Desktop (running)Git⚡ Quick Start1. Clone & SetupCreate a folder and place the project files inside.mkdir superapp-demo
cd superapp-demo
# (Copy the application files into this directory)
2. Launch the StackRun the entire infrastructure with one command.docker-compose up --build
Note: On the first run, the ollama-puller service will take 1-2 minutes to download the Phi-3 AI model (approx 2GB). Watch the logs for "Waiting for Ollama...".3. Test the PersonalizationOpen your browser to http://localhost:3000.Scenario A (Default):Context: "I am rushing to a meeting"Result: AI suggests "Quick Coffee Grab" (Orange Button).Scenario B (Evening/Relaxed):URL: http://localhost:3000/?context=It is a cozy rainy evening at homeResult: AI suggests "Comfort Food Delivery" (Purple Button).📂 Project Structuresuperapp-demo/
├── docker-compose.yml       # Orchestrates Java, Node, Mongo, Ollama
├── mongo-seed/
│   └── seed.js              # Pre-populates UI templates in MongoDB
├── backend/                 # Java 21 Spring Boot Application
│   ├── Dockerfile           # Multi-stage build (Maven -> JRE)
│   ├── pom.xml              # Dependencies (LangChain4j, Spring Data)
│   └── src/                 # Business Logic & AI Integration
└── frontend/                # Node.js Application
    ├── Dockerfile           # Alpine Node image
    ├── package.json         # Node dependencies
    ├── server.js            # Express Server
    └── views/               # EJS Templates (The "Phone Frame" UI)
🔧 Technology HighlightsJava 21 & Virtual ThreadsWe explicitly enable Virtual Threads in application.properties:spring.threads.virtual.enabled=true
This allows the backend to handle thousands of concurrent AI/DB requests without blocking OS threads, making it ideal for high-scale Superapps.Server-Driven UI (SDUI)The Frontend contains zero business logic. It doesn't know what a "Coffee Button" is until the backend sends the instruction:{
  "type": "HeroButton",
  "props": { "label": "Morning Brew", "color": "#FF8C00", "action": "..." }
}
This allows you to change the entire app interface instantly without an App Store update.🛑 TroubleshootingIssueSolution"Backend failed to connect to MongoDB"Ensure docker-compose is running. If restarting, use docker-compose down -v to reset volumes."Error: Brain Offline" in UIThe Java backend is likely still starting up or downloading the AI model. Wait 60 seconds and refresh.Docker Permission Error (Mac)Enable "File Sharing" for your project folder in Docker Desktop Settings -> Resources.📜 LicenseThis project is open-source and available under the MIT License.