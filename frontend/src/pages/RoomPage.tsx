import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

// Define participant type
interface Participant {
  userId: string;
  username: string;
  joinedAt?: string;
}

interface LanguageChangePayload {
  userId: string | undefined;
  language: string;
}

// Room interface definition
interface Room {
  id?: string;
  title: string;
  content?: string;
  language?: string;
  expiresAt?: string;
  participants?: Participant[];
}

// Interface for code change payload
interface CodeChangePayload {
  userId: string | undefined;
  content: string;
  timestamp: string;
}

const RoomPage: React.FC = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const joinSent = useRef(false);

  // Initialize with proper TypeScript types
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [room, setRoom] = useState<Room | null>(null);
  const [code, setCode] = useState<string>('');
  const [language, setLanguage] = useState<string>('javascript');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [stompClient, setStompClient] = useState<Client | null>(null);

  // Fetch room details
  useEffect(() => {
    const fetchRoomDetails = async () => {
      try {
        if (!roomId) {
          setError("Room ID is missing");
          setLoading(false);
          return;
        }

        setLoading(true);
        const response = await axios.get(`/api/rooms/${roomId}`);

        if (response.data) {
          setRoom(response.data);
          setCode(response.data.content || '');
          setLanguage(response.data.language || 'javascript');

          // If there are participants in the response, set them
          if (Array.isArray(response.data.participants)) {
            setParticipants(response.data.participants);
          }
        } else {
          setError("Invalid room data received");
        }

        setLoading(false);
      } catch (error) {
        console.error("Error fetching room details", error);
        setError("Failed to load room. Please try again.");
        setLoading(false);
      }
    };

    if (roomId) {
      fetchRoomDetails();
    } else {
      setError("Room ID is missing");
      setLoading(false);
    }
  }, [roomId]);

  // Add debugging for participants updates
  useEffect(() => {
    console.log("Participants updated:", participants);
  }, [participants]);
  // Connect to WebSocket
  useEffect(() => {
    if (!roomId || !user) return;

    try {
      const socket = new SockJS('/code-editor-ws');

      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log(str),
        onConnect: () => {
          console.log("Connected to WebSocket");

          // Subscribe to code changes
          client.subscribe(`/topic/rooms/${roomId}`, (message) => {
            try {
              const codeChange = JSON.parse(message.body);
              if (codeChange.userId !== user.id) {
                setCode(codeChange.content);
              }
            } catch (e) {
              console.error("Error parsing code update", e);
            }
          });

          // Add language change handler in the useEffect WebSocket setup
          client.subscribe(`/topic/rooms/${roomId}/language`, (message) => {
            try {
              const languageChange = JSON.parse(message.body);
              if (languageChange.userId !== user.id) {
                setLanguage(languageChange.language);
              }
            } catch (e) {
              console.error("Error parsing language update", e);
            }
          });

          // Subscribe to participant updates
          client.subscribe(`/topic/rooms/${roomId}/participants`, (message) => {
            try {
              const participantData = JSON.parse(message.body);
              if (Array.isArray(participantData)) {
                setParticipants(participantData);
              }
            } catch (e) {
              console.error("Error parsing participant update", e);
            }
          });
          // Send join notification only once
          if (!joinSent.current) {
            joinSent.current = true;
            client.publish({
              destination: `/app/rooms/${roomId}/join`,
              body: JSON.stringify({
                userId: user.id,
                username: user.username,
                joinedAt: new Date().toISOString()
              })
            });
            console.log("Join message sent for room", roomId);
          }
          // Send join notification
          client.publish({
            destination: `/app/rooms/${roomId}/join`,
            body: JSON.stringify({
              userId: user.id,
              username: user.username,
              joinedAt: new Date().toISOString()
            })
          });
        },
        onStompError: (frame) => {
          console.error('STOMP error', frame);
          setError("Connection error. Please refresh the page.");
        },
        onWebSocketError: (evt) => {
          console.error('WebSocket error', evt);
        }
      });

      setStompClient(client);
      client.activate();

      return () => {
        if (client && client.connected) {
          try {
            // Send leave notification
            client.publish({
              destination: `/app/rooms/${roomId}/leave`,
              body: JSON.stringify({
                userId: user.id
              })
            });
          } catch (e) {
            console.error("Error sending leave notification", e);
          }
          client.deactivate();
        }
      };
    } catch (error) {
      console.error("WebSocket connection error", error);
      setError("Failed to connect to real-time service. Please refresh.");
      return () => { }; // Return empty function as cleanup
    }
  }, [roomId, user]);
  // Update the language change handler for the select dropdown
  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLanguage = e.target.value;
    setLanguage(newLanguage);

    // Send language updates via WebSocket
    if (stompClient && stompClient.connected && roomId) {
      try {
        stompClient.publish({
          destination: `/app/rooms/${roomId}/language-change`,
          body: JSON.stringify({
            userId: user?.id,
            language: newLanguage
          } as LanguageChangePayload)
        });
      } catch (error) {
        console.error("Error sending language update", error);
      }
    }
  };
  const handleCodeChange = (value: string | undefined) => {
    if (value === undefined) return;

    setCode(value);

    // Send code updates via WebSocket
    if (stompClient && stompClient.connected && roomId) {
      try {
        stompClient.publish({
          destination: `/app/rooms/${roomId}/code-change`,
          body: JSON.stringify({
            userId: user?.id,
            content: value,
            timestamp: new Date().toISOString()
          } as CodeChangePayload)
        });
      } catch (error) {
        console.error("Error sending code update", error);
      }
    }
  };

  if (loading) {
    return <div className="loading-screen">Loading editor...</div>;
  }

  if (error) {
    return (
      <div className="error-container">
        <h2>Error</h2>
        <p>{error}</p>
        <button onClick={() => navigate('/')}>Go to Homepage</button>
      </div>
    );
  }

  return (
    <div className="room-container">
      <div className="room-header">
        <div className="room-info">
          <h1>{room?.title || 'Code Editor'}</h1>
          <p>Room ID: {roomId}</p>
          {room?.expiresAt && (
            <span className="room-expires">
              Expires: {new Date(room.expiresAt).toLocaleDateString()}
            </span>
          )}
        </div>
        <div className="room-controls">
          <select
            value={language}
            onChange={handleLanguageChange}
          >
            <option value="javascript">JavaScript</option>
            <option value="typescript">TypeScript</option>
            <option value="python">Python</option>
            <option value="java">Java</option>
            <option value="csharp">C#</option>
          </select>
          <button className="share-button" onClick={() => {
            navigator.clipboard.writeText(window.location.href);
            alert('Room link copied to clipboard!');
          }}>
            Share Room
          </button>
        </div>
      </div>

      <div className="editor-container">
        <div className="participants-sidebar">
          <h3>Participants ({participants?.length || 0})</h3>
          <ul>
            {Array.isArray(participants) && participants.length > 0 ? (
              participants.map((participant: Participant) => (
                <li
                  key={participant.userId || Math.random().toString()}
                  className={participant.userId === user?.id ? 'current-user' : ''}
                >
                  {participant.username || 'Anonymous'}
                  {participant.userId === user?.id && (
                    <span className="you-label">You</span>
                  )}
                </li>
              ))
            ) : (
              <li>No participants yet</li>
            )}
          </ul>
        </div>

        <div className="code-editor-wrapper">
          <Editor
            height="100%"
            language={language}
            value={code}
            onChange={handleCodeChange}
            theme="vs-dark"
            options={{
              fontSize: 14,
              minimap: { enabled: true },
              automaticLayout: true
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default RoomPage;