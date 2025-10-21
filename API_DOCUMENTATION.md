# HUB Backend API Documentation

## Chat Messages API

### Overview
The Chat Messages API allows Home Owners and Consultants to exchange text messages and voice notes during approved consultations.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Send Message
**POST** `/api/v1/chats/{consultationId}/messages`

Send a text message or voice note in a consultation chat.

**Parameters:**
- `consultationId` (path): UUID of the consultation
- `messageType` (form): "TEXT" or "VOICE"
- `messageContent` (form, optional): Text content for TEXT messages
- `voiceFile` (form, optional): Multipart file for VOICE messages (MP3/M4A only, max 10MB)

**Request Example (Text Message):**
```bash
curl -X POST "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "messageType=TEXT" \
  -F "messageContent=Hello, I have a question about the design"
```

**Request Example (Voice Message):**
```bash
curl -X POST "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "messageType=VOICE" \
  -F "voiceFile=@voice_note.mp3"
```

**Response (200 OK):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "consultationId": "123e4567-e89b-12d3-a456-426614174000",
  "senderId": 1,
  "receiverId": 2,
  "messageType": "TEXT",
  "messageContent": "Hello, I have a question about the design",
  "fileUrl": null,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### 2. Get Chat Messages
**GET** `/api/v1/chats/{consultationId}/messages`

Retrieve all messages in a consultation chat, ordered by creation time.

**Parameters:**
- `consultationId` (path): UUID of the consultation

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "consultationId": "123e4567-e89b-12d3-a456-426614174000",
    "senderId": 1,
    "receiverId": 2,
    "messageType": "TEXT",
    "messageContent": "Hello, I have a question about the design",
    "fileUrl": null,
    "createdAt": "2024-01-15T10:30:00Z"
  },
  {
    "id": "567e8901-e89b-12d3-a456-426614174002",
    "consultationId": "123e4567-e89b-12d3-a456-426614174000",
    "senderId": 2,
    "receiverId": 1,
    "messageType": "VOICE",
    "messageContent": null,
    "fileUrl": "/hub-files/chat/voice/voice_note.mp3",
    "createdAt": "2024-01-15T10:31:00Z"
  }
]
```

#### 3. Get Latest Message
**GET** `/api/v1/chats/{consultationId}/messages/latest`

Retrieve the most recent message in a consultation chat.

**Parameters:**
- `consultationId` (path): UUID of the consultation

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages/latest" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "567e8901-e89b-12d3-a456-426614174002",
  "consultationId": "123e4567-e89b-12d3-a456-426614174000",
  "senderId": 2,
  "receiverId": 1,
  "messageType": "VOICE",
  "messageContent": null,
  "fileUrl": "/hub-files/chat/voice/voice_note.mp3",
  "createdAt": "2024-01-15T10:31:00Z"
}
```

### Error Responses

**400 Bad Request:**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Message content is required for text messages"
}
```

**403 Forbidden:**
```json
{
  "errorCode": "ACCESS_DENIED",
  "message": "Unauthorized to send message in this consultation"
}
```

**404 Not Found:**
```json
{
  "errorCode": "NOT_FOUND",
  "message": "Consultation not found"
}
```

### Business Rules

1. **Consultation Status**: Chat is only allowed when consultation status is `AKTIF` (approved).
2. **User Authorization**: Only Home Owner and Consultant associated with the consultation can access the chat.
3. **File Upload**: Voice notes must be MP3 or M4A format, maximum 10MB.
4. **Message Types**: Support TEXT and VOICE message types.
5. **Real-time**: Currently REST API only; WebSocket implementation pending.

### Data Models

#### ChatMessageDTO
```json
{
  "id": "UUID",
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)",
  "createdAt": "Instant"
}
```

## WebSocket Real-Time Chat API (In-Memory Broker)

### Overview
The WebSocket API provides **fully real-time chat functionality** using **STOMP over WebSocket with Spring's built-in simple broker**. No external message broker (RabbitMQ, Kafka, Redis) is required - all communication is handled in-memory within the Spring application.

### Architecture Notes
- **Broker**: Spring WebSocket Simple Broker (in-memory)
- **Protocol**: STOMP over WebSocket
- **Authentication**: JWT token validation on WebSocket handshake
- **Real-time**: Instant message delivery without external dependencies

### WebSocket Configuration
- **Endpoint**: `ws://localhost:8080/ws/chat`
- **STOMP Application Prefix**: `/app`
- **Broker Destinations**:
  - `/topic/chat/{consultationId}` - Chat room messages
  - `/topic/chat/{consultationId}/read` - Read status updates
  - `/queue/notifications/{userId}` - Private notifications

### Authentication
WebSocket connections require JWT authentication passed in the `Authorization` header during the STOMP CONNECT frame.

### STOMP Message Flow

#### 1. Connect to WebSocket
```javascript
const socket = new SockJS('/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, function(frame) {
    console.log('Connected: ' + frame);
}, function(error) {
    console.log('Connection error: ' + error);
});
```

#### 2. Subscribe to Chat Room
```javascript
// Subscribe to chat messages
stompClient.subscribe('/topic/chat/' + consultationId, function(message) {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
    // Update UI immediately
});

// Subscribe to read status updates
stompClient.subscribe('/topic/chat/' + consultationId + '/read', function(message) {
    const readStatus = JSON.parse(message.body);
    console.log('Read status:', readStatus);
    // Update read receipts
});

// Subscribe to notifications
stompClient.subscribe('/user/queue/notifications', function(notification) {
    const notificationData = JSON.parse(notification.body);
    console.log('Notification:', notificationData);
    // Show notification badge
});
```

#### 3. Send Messages (Real-time)
```javascript
// Send text message - appears instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/send', {}, JSON.stringify({
    consultationId: consultationId,
    senderId: currentUserId,
    receiverId: otherUserId,
    messageType: 'TEXT',
    messageContent: 'Hello, how are you?',
    fileUrl: null
}));

// Send voice message - appears instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/send', {}, JSON.stringify({
    consultationId: consultationId,
    senderId: currentUserId,
    receiverId: otherUserId,
    messageType: 'VOICE',
    messageContent: null,
    fileUrl: '/hub-files/chat/voice/audio.mp3'
}));
```

#### 4. Mark Messages as Read (Real-time)
```javascript
// Mark as read - updates instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/read/' + messageId, {}, {});
```

### Message Payloads

#### ChatMessagePayload (WebSocket Send)
```json
{
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)"
}
```

#### ChatMessageResponse (WebSocket Receive)
```json
{
  "id": "UUID",
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)",
  "isRead": "Boolean",
  "createdAt": "Instant"
}
```

#### Notification Payload (WebSocket)
```json
{
  "type": "NEW_MESSAGE",
  "consultationId": "UUID",
  "senderId": "Long",
  "messageType": "TEXT|VOICE",
  "createdAt": "Instant"
}
```

#### Read Status Payload (WebSocket)
```json
{
  "messageId": "UUID",
  "isRead": true,
  "readBy": "Long"
}
```

### Business Rules

1. **Authentication**: JWT token required for WebSocket connection
2. **Authorization**: Only consultation participants can join chat room
3. **Consultation Status**: Only `AKTIF` consultations allow chat
4. **Real-time Updates**: All messages broadcast instantly to `/topic/chat/{consultationId}`
5. **Read Receipts**: Messages marked as read update instantly for all subscribers
6. **Notifications**: New messages trigger instant notifications to receivers
7. **In-Memory**: No external broker - all routing handled by Spring WebSocket

### Error Handling

WebSocket errors are logged server-side. Client should implement reconnection logic:

```javascript
stompClient.onStompError = function(frame) {
    console.log('Broker reported error: ' + frame.headers['message']);
    console.log('Additional details: ' + frame.body);
};

// Reconnection logic
function connect() {
    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);
    // ... connection setup
}

stompClient.onWebSocketClose = function() {
    setTimeout(connect, 5000); // Reconnect after 5 seconds
};
```

### Integration with REST API

The WebSocket implementation works alongside the existing REST API:
- **REST API**: Initial data loading, message history, mark as read
- **WebSocket**: Real-time messaging, instant updates
- **Same Business Logic**: Validation and authorization consistent
- **Fallback Support**: REST API works when WebSocket unavailable

### Testing Real-time Functionality

#### 1. Start Application
```bash
mvn spring-boot:run
```

#### 2. Test WebSocket Connection
```javascript
// In browser console or test script
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
}, function() {
    console.log('Connected to real-time chat!');
});
```

#### 3. Test Message Sending
```javascript
// Send test message
stompClient.send('/app/chat/CONSULTATION_ID/send', {}, JSON.stringify({
    consultationId: 'CONSULTATION_ID',
    senderId: 1,
    receiverId: 2,
    messageType: 'TEXT',
    messageContent: 'Real-time test message!',
    fileUrl: null
}));
```

#### 4. Verify Instant Delivery
- Message should appear immediately in all subscribed clients
- No external broker required
- Uses Spring's in-memory simple broker

#### CreateChatMessageRequest
```json
{
  "messageType": "TEXT|VOICE",
  "messageContent": "String (optional for TEXT)",
  "voiceFile": "MultipartFile (required for VOICE)"
}