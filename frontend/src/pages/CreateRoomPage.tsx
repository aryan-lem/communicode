import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Card } from '../components/Card';
import { motion } from 'framer-motion';
import { useAuth } from '../context/AuthContext';

const CreateRoomPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [title, setTitle] = useState<string>('');
  const [language, setLanguage] = useState<string>('javascript');
  const [customId, setCustomId] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!title.trim()) {
      setError('Please enter a room title');
      return;
    }
    
    try {
      setIsSubmitting(true);
      setError(null);
      
      const response = await axios.post('/api/rooms', {
        title,
        language,
        customId: customId.trim() || undefined
      });
      
      // Navigate to the newly created room
      navigate(`/room/${response.data.id}`);
    } catch (err: any) {
      console.error('Error creating room', err);
      setError(err.response?.data?.message || 'Failed to create room');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  return (
    <div className="modern-container">
      <div className="background-gradient"></div>
      <div className="background-shapes">
        <motion.div 
          className="shape shape-1"
          animate={{
            y: [0, -15, 0],
            rotate: [0, 5, 0],
          }}
          transition={{
            duration: 6,
            repeat: Infinity,
            ease: "easeInOut"
          }}
        />
        <motion.div 
          className="shape shape-2"
          animate={{
            y: [0, 20, 0],
            rotate: [0, -7, 0],
          }}
          transition={{
            duration: 7,
            repeat: Infinity,
            ease: "easeInOut"
          }}
        />
      </div>
      
      <Card>
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.6 }}
        >
          <div className="app-logo">
            <div className="logo-circle"></div>
          </div>
          
          <motion.h1 
            className="app-title"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
          >
            Create <span>Code Room</span>
          </motion.h1>
          
          <motion.p 
            className="app-description"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5, duration: 0.5 }}
          >
            Set up a collaborative coding environment
          </motion.p>
          
          <motion.form 
            onSubmit={handleSubmit}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.7, duration: 0.5 }}
          >
            <motion.div
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.8 }}
            >
              <input
                type="text"
                value={title}
                onChange={e => setTitle(e.target.value)}
                placeholder="Room Title"
                className="input"
                required
              />
              
              <div className="select-container">
                <label htmlFor="language">Programming Language</label>
                <select 
                  id="language"
                  value={language}
                  onChange={e => setLanguage(e.target.value)}
                  className="input"
                >
                  <option value="javascript">JavaScript</option>
                  <option value="typescript">TypeScript</option>
                  <option value="python">Python</option>
                  <option value="java">Java</option>
                  <option value="csharp">C#</option>
                  <option value="cpp">C++</option>
                  <option value="php">PHP</option>
                  <option value="ruby">Ruby</option>
                  <option value="go">Go</option>
                  <option value="rust">Rust</option>
                  <option value="html">HTML</option>
                  <option value="css">CSS</option>
                </select>
              </div>
              
              <input
                type="text"
                value={customId}
                onChange={e => setCustomId(e.target.value)}
                placeholder="Custom Room ID (optional)"
                className="input"
                pattern="^[a-zA-Z0-9-_]*$"
                title="Letters, numbers, hyphens, and underscores only"
              />
              
              {error && (
                <motion.div 
                  className="error-text"
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                >
                  {error}
                </motion.div>
              )}
              
              <motion.div 
                className="room-tips"
                initial={{ opacity: 0 }}
                animate={{ opacity: 0.8 }}
                transition={{ delay: 1 }}
              >
                <p>Room will expire after 7 days of inactivity</p>
              </motion.div>
              
              <motion.button
                type="submit"
                className="button-primary"
                disabled={isSubmitting}
                whileHover={{ scale: 1.03 }}
                whileTap={{ scale: 0.98 }}
              >
                {isSubmitting ? (
                  <div className="loading-circle"></div>
                ) : (
                  'Create Room'
                )}
              </motion.button>
            </motion.div>
          </motion.form>
        </motion.div>
      </Card>
    </div>
  );
};

export default CreateRoomPage;