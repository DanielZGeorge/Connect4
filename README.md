# Connect4 Online
 
## Prerequisites
 
This project is built using Apache Maven. Maven requires a **Java Development Kit (JDK)** — version 8 or later. Verify by running:
 
```bash
java -version
```
 
If you don't have a JDK installed, download one from [Adoptium](https://adoptium.net/) before proceeding.
 
---
 
## macOS
 
### Option 1: Homebrew (Recommended)
 
1. Install [Homebrew](https://brew.sh) if you haven't already.
2. Run:
   ```bash
   brew install maven
   ```
3. Verify the installation:
   ```bash
   mvn -version
   ```
 
### Option 2: Manual Install
 
1. Download the latest binary `.tar.gz` from [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
2. Extract it to a directory of your choice:
   ```bash
   tar -xzf apache-maven-3.*-bin.tar.gz -C /opt
   ```
3. Add Maven to your shell profile (`~/.zshrc` or `~/.bash_profile`):
   ```bash
   export M2_HOME=/opt/apache-maven-3.<version>
   export PATH="$M2_HOME/bin:$PATH"
   ```
4. Reload your shell and verify:
   ```bash
   source ~/.zshrc
   mvn -version
   ```
 
---
 
## Windows
 
### Option 1: Manual Install
 
1. Download the latest binary `.zip` from [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
2. Extract it to a directory such as `C:\Program Files\Apache\maven`.
3. Set environment variables:
   - Open **Settings → System → About → Advanced system settings → Environment Variables**.
   - Under **System variables**, click **New** and add:
     - Variable name: `M2_HOME`
     - Variable value: `C:\Program Files\Apache\maven\apache-maven-3.<version>`
   - Find the **Path** variable, click **Edit**, and add a new entry:
     ```
     %M2_HOME%\bin
     ```
4. Open a **new** Command Prompt or PowerShell and verify:
   ```cmd
   mvn -version
   ```

## Verifying Your Installation
 
A successful `mvn -version` output will look something like:
 
```
Apache Maven 3.9.x
Maven home: /opt/apache-maven-3.9.x
Java version: 21.0.x, vendor: Eclipse Adoptium
```
 
Confirm that the **Java version** shown matches the JDK you intend to use. If it doesn't, set the `JAVA_HOME` environment variable to point to the correct JDK installation directory.

## Run the Project

To run the frontend, run:

```
cd Frontend
mvn package javafx:run
```

To run the backend servlets, run:

```
cd backend
mvn package cargo:run
```

To run the NIO server, run NioServerMain.java as a java application.

## Backend Overview
- REST endpoints:
  - `GET /backend/leaderboard?page=1&size=20&q=alice`
  - `GET /backend/active-matches`
- WebSocket endpoint:
  - `ws://<host>:<port>/backend/ws`

## WebSocket Messages
All messages use `type;sender;target;payload`.

Client → Server:
- `HELLO;username;server;role=guest` or `HELLO;username;server;role=registered`
- `MATCH;username;server;ranked`
- `BOT;username;server;difficulty=easy|medium|hard`
- `SPECTATE;username;targetPlayer;watch`
- `MOVE;username;server;col=3`
- `CHAT;username;opponent;Hello`

Server → Client:
- `STATE;server;<gameId>;<json snapshot>`
- `ACK;server;client;<payload>`
- `ERROR;server;<type>;<reason>`

Snapshot payload includes:
- `gameId`, `players`, `currentPlayer`, `turnEndsAt`, `winner`, `gameOver`, `board`

## Smoke Harness
Run `server.GameSessionSmoke` to exercise game-state logic without the server.

```
java -cp <classpath> server.GameSessionSmoke
```

