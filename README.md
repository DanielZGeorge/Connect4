Project Name: Competitive Online Connect 4
Course: CSCI 201
Current Date: April 21, 2026

1. Project Overview
The objective of this project is to develop a real-time, turn-based, multiplayer version of the classic game Connect 4. The platform facilitates competitive play by matching users based on a calculated Elo rating, while also supporting practice modes against AI and a spectator system for unauthenticated guests.

Core Technical Requirements:
Backend: A multi-threaded Java server using Non-blocking I/O (NIO) for concurrent network management.
Frontend: A JavaFX-based graphical user interface (GUI).
Database: A relational database (MySQL) to manage user accounts, authentication, match history, and Elo rankings.
Concurrency: Robust thread management for game sessions, matchmaking, and real-time chat.

2. Milestone Progress (As of April 21, 2026)

Milestone: Project Proposal
Due Date: March 1
Status: Complete
Key Deliverables: Concept definition, team member names, and weekly meeting schedule.

Milestone: High-Level Requirements
Due Date: March 8
Status: Complete
Key Deliverables: Client-focused description of program behavior without technical details.

Milestone: Technical Specifications
Due Date: March 14
Status: Complete
Key Deliverables: Outline of technical tasks, architecture setup, and communication protocols.

Milestone: Detailed Design
Due Date: March 29
Status: Complete
Key Deliverables: Database schema, class diagrams, inheritance hierarchies, and AI algorithms (Minimax).

Milestone: Testing Plan & Code
Due Date: April 5
Status: Complete
Key Deliverables: Implementation of unit (JUnit), black box, white box, and regression tests.

Milestone: Deployment Document
Due Date: April 12
Status: Complete
Key Deliverables: Instructions for database configuration, server deployment, and client execution.

Milestone: Final Code Submission
Due Date: May 1
Status: In Progress
Key Deliverables: Compiled documentation, final Java source code, and multi-threaded functionality.

Milestone: Presentation
Due Date: May 12
Status: Pending
Key Deliverables: Live demonstration and visual aids highlighting technical effectiveness.

3. Key Features & Implementation Details
Matchmaking System: Uses a dynamic Elo-based queue that pairs players of similar skill levels, with tolerance ranges that expand after 30 seconds of waiting to ensure matches are found.
AI Opponents: Three difficulty tiers:
Easy: Random move selection.
Medium: Greedy algorithm focusing on immediate wins or blocks.
Hard: Minimax algorithm with alpha-beta pruning for deep-tree state evaluation.
User Roles:
Guests: Can access the public leaderboard and spectate live matches in real-time.
Registered Users: Can join ranked queues, track their Elo, chat with opponents, and access private lobbies.
Real-Time Chat: A dedicated thread-safe messaging system restricted to active participants within a match.

4. Current Development Status
The project has moved past the primary documentation and testing phases. All structural designs and deployment procedures have been established.
Infrastructure: The server architecture (NIO) and database schema (Users and Match_History tables) are fully designed.
Frontend: A starter JavaFX framework is initialized, featuring a ViewManager for scene switching and a mock backend for testing UI navigation without active server requirements.
Testing: Core JUnit tests for win-detection and Elo math are ready for implementation within the final codebase.
Next Priority: Final code integration and preparation for the project demonstration on May 12.
