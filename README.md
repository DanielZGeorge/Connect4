# Connect4 Online

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

