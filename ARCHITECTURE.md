Architecture Definition Document: Adaptive UI Superapp

1. Introduction

This document provides the architectural blueprint for the Adaptive UI Superapp, a contextual, AI-driven application leveraging a Server-Driven UI (SDUI) paradigm. It outlines both the High-Level Architecture (HLA) and Low-Level Architecture (LLA), defining system components, data flows, and infrastructure design.

1.1 Purpose

The purpose of this architecture is to decouple the user interface from the client application, allowing the backend to dynamically generate and serve UI layouts (JSON schemas) customized in real-time by a Small Language Model (SLM) based on user telemetry and context.

1.2 Rationale for Framework Selection

The technology stack was strategically selected to maximize scalability, reduce operational costs, and ensure data privacy:

Java 21 & Virtual Threads (Project Loom): Chosen for its ability to handle immense scale and I/O-heavy operations (like querying LLMs and databases) using a simple synchronous programming model, avoiding the debugging complexity of reactive frameworks (e.g., WebFlux).

Server-Driven UI (SDUI): Eliminates the need for frequent app store updates, allowing immediate, personalized UX rollouts across all client platforms simultaneously.

Local SLM (Ollama/Phi-3): Selected over cloud LLMs (OpenAI/Anthropic) to guarantee zero external network latency, eliminate recurring token costs, and enforce strict data privacy by keeping all inference in-house.

MongoDB: The schema-less, JSON-native structure perfectly aligns with the highly dynamic and nested nature of SDUI component payloads.

2. High-Level Architecture (HLA)

The High-Level Architecture defines the macroscopic view of the system, dividing it into four primary, containerized microservices operating within an isolated Docker network.

2.1 System Components

Frontend Renderer (Node.js / Express): A lightweight "Backend-For-Frontend" (BFF) that serves as a dumb renderer. It holds no business logic, serving only to capture user context (e.g., location, time, implicit intent) and parse the JSON responses from the Core Backend into native/web view components.

Core Backend Orchestrator (Java 21 / Spring Boot): The central processing unit of the application. It routes traffic, orchestrates data retrieval, and manages the synchronous communication between the AI Engine and the Database. Designed for extreme concurrency using Project Loom (Virtual Threads).

Document Store (MongoDB 7.0): A NoSQL database optimized for high-speed retrieval of flexible JSON-like documents representing UI "Lego blocks" and user behavioral data.

Local AI Engine (Ollama / Phi-3): A self-hosted Small Language Model (SLM) responsible for intent classification and dynamic micro-copywriting.

2.2 Detailed System Component Diagram

graph TD
    User[Client Device / Browser] -->|HTTP GET /?context=...| NodeApp

    subgraph Docker Internal Network [Docker Bridged Network]
        
        subgraph Frontend [Frontend Renderer Node.js:3000]
            NodeApp[Express Server]
            EJSEngine[EJS Templating Engine]
            NodeApp -->|Route parsing| EJSEngine
        end

        subgraph Backend [Core Orchestrator Java 21:8080]
            Dispatcher[Tomcat / Virtual Threads]
            UIController[UI Controller]
            LayoutService[Dynamic Layout Service]
            AICopywriter[UI Copywriter @AiService]
            
            Dispatcher --> UIController
            UIController --> LayoutService
            LayoutService <--> AICopywriter
        end

        subgraph Database [MongoDB 7.0:27017]
            DB[(superapp DB)]
            Coll[\ui_components collection\]
            DB --- Coll
        end

        subgraph AIEngine [Local AI Engine Ollama:11434]
            OllamaAPI[Ollama HTTP API]
            Phi3((Phi-3 Model Weights))
            OllamaAPI --- Phi3
        end

        %% Cross-component interactions
        NodeApp -->|REST GET /api/ui| Dispatcher
        LayoutService <-->|Spring Data / BSON| DB
        AICopywriter <-->|LangChain4j REST| OllamaAPI
        
    end
    
    EJSEngine -.->|Returns HTML/JSON payload| User


3. Low-Level Architecture (LLA)

The Low-Level Architecture delves into the internal mechanics, design patterns, and schemas of the individual services.

3.1 Backend Internal Design (Java 21)

The backend utilizes a classic Controller-Service-Repository pattern, enhanced with LangChain4j for AI integration.

UIController: Exposes the /api/ui endpoint. Accepts raw context strings.

Dummy Code Representation:

@RestController
@RequestMapping("/api/ui")
public class UIController {
    private final DynamicLayoutService layoutService;

    public UIController(DynamicLayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @GetMapping
    public ResponseEntity<List<UIComponent>> getLayout(@RequestParam(defaultValue = "Normal day") String context) {
        // Dummy code illustrating the entry point
        List<UIComponent> layout = layoutService.getLayout(context);
        return ResponseEntity.ok(layout);
    }
}


DynamicLayoutService: The primary orchestrator. Contains the logic to:

Determine the baseline intent (via heuristics or AI).

Fetch the corresponding baseline UI template from MongoDB.

Call the UICopywriter service to generate personalized labels.

Hydrate the template with the AI-generated labels.

UICopywriter (AI Interface): An interface annotated with LangChain4j's @AiService. It binds system prompts and user messages to the Ollama HTTP API, abstracting the LLM interaction into standard Java method calls.

Concurrency Model: Bypasses traditional OS thread pooling by using spring.threads.virtual.enabled=true. Each incoming UI request, database call, and LLM inference block runs on a Virtual Thread, allowing the JVM to handle tens of thousands of concurrent layout generation requests without memory exhaustion.

3.2 Data Model (MongoDB)

The database operates on a dynamic schema. The primary collection is ui_components.

Schema Definition: ui_components

{
  "_id": "ObjectId / String (e.g., tpl_morning)",
  "tag": "String (Intent Mapping Tag, e.g., CAFFEINE, DINNER)",
  "type": "String (UI Widget Identifier, e.g., HeroButton, Header)",
  "props": {
    "defaultGoal": "String (Prompt instruction for AI)",
    "color": "String (Hex code)",
    "action": "String (Deep link URI)",
    "label": "String (Optional/Overwritten by AI)"
  }
}


3.3 Frontend SDUI Parsing (Node.js / EJS)

The frontend utilizes Express to handle routing and EJS (Embedded JavaScript) for templating.

Routing: The Express server captures query parameters (?context=...) and forwards them to the Java API.

Dummy Code Representation:

app.get('/', async (req, res) => {
    // Dummy code illustrating signal capture and backend request
    const userContext = req.query.context || "I am rushing to a meeting";
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';

    try {
        const response = await axios.get(`${backendUrl}/api/ui?context=${encodeURIComponent(userContext)}`);
        res.render('index', { components: response.data, context: userContext });
    } catch (error) {
        console.error("Backend Error:", error.message);
        res.render('index', { components: [], context: "Error: Brain Offline" });
    }
});


Rendering Logic: The EJS template iterates over the JSON array returned by Java. It uses conditional switching (if (comp.type === 'HeroButton')) to map the abstract JSON definition to concrete HTML/CSS elements.

4. Interaction & Sequence Flow

The following sequence illustrates the microsecond-level interactions required to serve a single personalized screen.

sequenceDiagram
    participant User
    participant Node as Frontend (Node.js)
    participant Java as Backend (Spring Boot)
    participant AI as Ollama (Phi-3)
    participant DB as MongoDB

    User->>Node: Open App (Sends context "Rushing to meeting")
    Node->>Java: GET /api/ui?context="Rushing to meeting"
    
    Note over Java: Phase 1: Intent Matching
    Java->>Java: Analyze Time/Context -> Intent = "CAFFEINE"
    
    Note over Java, DB: Phase 2: Template Retrieval
    Java->>DB: findOne(tag == "CAFFEINE")
    DB-->>Java: Returns JSON {type: "HeroButton", defaultGoal: "Get coffee"}
    
    Note over Java, AI: Phase 3: AI Copywriting
    Java->>AI: Prompt: Context="Rushing...", Goal="Get coffee"
    AI-->>Java: Returns text: "Quick Coffee Grab"
    
    Note over Java: Phase 4: Hydration
    Java->>Java: Mutate JSON -> props.label = "Quick Coffee Grab"
    
    Java-->>Node: Returns Hydrated SDUI JSON Array
    Node->>Node: Parse JSON array to HTML elements
    Node-->>User: Renders Custom UI


5. Deployment Architecture

The system is designed for containerized deployment, orchestrated via Docker Compose for local environments (extensible to Kubernetes for production).

Networking: All services run on a custom bridged Docker network.

Service Discovery: Hardcoded internal DNS via Docker Compose service names (e.g., mongodb:27017, ollama:11434, backend:8080).

Volume Mounts:

mongo_data: Persists UI components and user vectors across container restarts.

ollama_data: Caches the downloaded ~2GB Phi-3 model weights so they are not re-downloaded upon recreation.

Initialization: A mongo-seed script is bound to MongoDB's docker-entrypoint-initdb.d to automatically inject the baseline UI templates upon the first successful boot.

5.1 Prerequisites

To successfully deploy and run this architecture, the following prerequisites must be met:

Hardware Requirements: Minimum 4 CPU cores, 16GB RAM (critical for running the JVM and caching the ~2GB Phi-3 SLM in memory simultaneously), and 10GB of available disk space.

Software (Dockerized): Docker Engine v24.0+ and Docker Compose v2.0+.

Software (Local Development): Java Development Kit (JDK) 21+, Node.js v20+, and Maven 3.9+.

6. Non-Functional Requirements (NFRs)

Scalability: The Node.js and Java 21 containers are entirely stateless and can be scaled horizontally. MongoDB can be transitioned to a sharded cluster.

Latency: Target backend response time is < 300ms. Local SLM inference (Phi-3) is utilized explicitly to avoid the network latency and rate limits associated with external cloud LLMs (like OpenAI/Anthropic).

Privacy & Security: 100% of user context strings and behavioral data remain within the private Docker network. No data is transmitted to third-party AI providers.

Resilience: The backend implements defensive programming; if the AI Engine times out or fails, the backend falls back to providing the static defaultGoal string from MongoDB to ensure the UI always renders.

7. Safety and Security Requirements

To ensure a robust and secure superapp environment, the following safety guidelines are strictly enforced:

Prompt Injection Protection: All raw user context strings originating from the client must be sanitized and validated prior to being passed to the local SLM to prevent malicious prompt overrides or logical bypasses.

Data Isolation: Because intent classification is performed by a local SLM, no Personally Identifiable Information (PII) or contextual telemetry ever leaves the internal Docker network boundaries. External API calls are strictly forbidden for intent resolution.

Container Network Security: The MongoDB and Ollama containers must not bind to public host ports in production environments. They should remain completely isolated and only accessible internally by the Java Orchestrator.

Resource Quotas & Rate Limiting: Due to the high computational cost of local LLM inference, aggressive API rate limiting, circuit breakers, and timeouts must be configured on the UIController to prevent Denial of Service (DoS) and host CPU exhaustion.

8. Vision and Future Development

The Adaptive UI Superapp is designed as a foundational, evolving ecosystem. The roadmap for future development includes:

Phase 1: Advanced Agentic Workflows: Transitioning the SLM from purely generating UI labels to executing complex, multi-step backend actions (e.g., autonomously booking rides or ordering food based on intent).

Phase 2: Multi-Modal Context Awareness: Upgrading the context capture engine to process voice inputs, device accelerometer data, and camera vision (via models like LLaVA) to enrich the UI generation context.

Phase 3: Edge AI Transition: Shrinking the SLM further to deploy directly onto the client mobile device (via ONNX runtime or ExecuTorch), shifting compute costs away from the backend and enabling 100% offline adaptive UIs.

Phase 4: Decentralized Component Marketplace: Allowing third-party vendors to submit SDUI JSON "Lego blocks" into the MongoDB database, which the AI can autonomously select, combine, and render for users based on hyper-localized needs.