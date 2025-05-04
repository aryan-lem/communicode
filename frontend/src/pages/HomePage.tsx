import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import './HomePage.css'; 
const HomePage: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [roomId, setRoomId] = useState('');
  
  const handleCreateRoom = () => {
    navigate('/create-room');
  };
  
  const handleJoinRoom = (e: React.FormEvent) => {
    e.preventDefault();
    if (roomId.trim()) {
      navigate(`/room/${roomId.trim()}`);
    }
  };
  
  return (
    <div className="home-container">
      <nav className="navbar">
        <div className="logo">
          <div className="logo-circle"></div>
          <h1>communicode</h1>
        </div>
        
        <div className="user-info">
          {user && (
            <>
              <img src={user.avatarUrl || '/default-avatar.png'} alt={user.username || user.name} />
              <span>{user.username || user.name}</span>
              <button onClick={logout} className="logout-button">Logout</button>
            </>
          )}
        </div>
      </nav>
      
      <div className="hero-section">
        <motion.div 
          className="hero-content"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7 }}
        >
          <h1>Collaborative Coding in Real-Time</h1>
          <p>Create or join a code room to start collaborating instantly</p>
          
          <div className="action-buttons">
            <motion.button 
              className="create-button"
              onClick={handleCreateRoom}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              Create New Room
            </motion.button>
            
            <div className="divider">or</div>
            
            <form onSubmit={handleJoinRoom} className="join-form">
              <input 
                type="text"
                placeholder="Enter Room ID"
                value={roomId}
                onChange={(e) => setRoomId(e.target.value)}
                className="room-id-input"
              />
              <motion.button
                type="submit"
                className="join-button"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                Join Room
              </motion.button>
            </form>
          </div>
        </motion.div>
      </div>
      
      <div className="features-section">
        <div className="feature">
          <div className="feature-icon">🔄</div>
          <h3>Real-Time Collaboration</h3>
          <p>Code together with your team in real-time, see changes as they happen</p>
        </div>
        
        <div className="feature">
          <div className="feature-icon">🔒</div>
          <h3>Secure Rooms</h3>
          <p>Private rooms with unique IDs only accessible to those with the link</p>
        </div>
        
        <div className="feature">
          <div className="feature-icon">⏱️</div>
          <h3>Temporary Storage</h3>
          <p>Rooms automatically expire after 7 days of inactivity</p>
        </div>
      </div>
    </div>
  );
};

export default HomePage;