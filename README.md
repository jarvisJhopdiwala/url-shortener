# URL Shortener (LLD)

A robust, purely in-memory URL Shortener built in Java. This project demonstrates Low-Level Design (LLD) principles, focusing on clean architecture, thread safety, and custom hashing/encoding mechanics.

## Architecture

The system is designed with strict separation of concerns, allowing for easy migration from in-memory storage to distributed databases (e.g., Redis, PostgreSQL) in the future.

* **Core Services:** Handles MD5 hashing, Base62 encoding, and collision resolution via salting.
* **Storage Layer:** Utilizes `ConcurrentHashMap` for high-throughput, thread-safe, lock-free reads and writes.
* **Repository Layer:** Acts as a boundary between domain logic and raw storage, automatically filtering out expired records during read operations.
* **Scheduler:** A background `ScheduledExecutorService` that asynchronously sweeps the storage map to purge expired URLs and reclaim memory.

## Features

* **Custom Shortening:** Converts long URLs into 7-character Base62 alphanumeric strings.
* **Idempotency:** Submitting the same long URL multiple times returns the same short URL (preventing DB bloat).
* **TTL (Time-To-Live):** URLs accept a TTL in seconds and automatically expire.
* **Garbage Collection:** A background thread safely iterates and deletes expired entries without blocking active read/write traffic.
* **Collision Handling:** Gracefully handles hash collisions using an appending salt retry mechanism.

## Project Structure

```text
src/main/java/core/
├── Main.java                      # Application entry point
├── models/                        # DTOs (Request, Response, Record)
├── repository/                    # Abstraction over data access
├── scheduler/                     # Background cron jobs (Cleanup)
├── services/                      # Core business logic (Hashing, Encoding)
└── storage/                       # Data persistence implementations
    ├── Storage.java               # Interface for DB contracts
    └── local/HashMapStorage.java  # Thread-safe in-memory DB