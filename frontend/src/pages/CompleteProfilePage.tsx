// src/pages/CompleteProfilePage.tsx
import React, { useState, useEffect } from 'react';
import { Card } from '../components/Card';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import '../styles.css';

export default function CompleteProfilePage() {
  const { user, setUsername } = useAuth();
  const [localUsername, setLocalUsername] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  // Redirect if user already has a username
  useEffect(() => {
    if (user && user.username) {
      navigate('/');
    }
  }, [user, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    
    try {
      await setUsername(localUsername);
      // Show success animation before redirecting
      setTimeout(() => {
        navigate('/');
      }, 800);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Username not available');
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
            Welcome, <span>{user?.name || 'New User'}</span>
          </motion.h1>
          
          <motion.p 
            className="app-description"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5, duration: 0.5 }}
          >
            Create a unique username for your account
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
                value={localUsername}
                onChange={e => setLocalUsername(e.target.value)}
                placeholder="Choose a username"
                className="input"
                required
                minLength={3}
                maxLength={20}
                pattern="^[a-zA-Z0-9_]+$"
                title="Letters, numbers, and underscores only"
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
                className="username-tips"
                initial={{ opacity: 0 }}
                animate={{ opacity: 0.8 }}
                transition={{ delay: 1 }}
              >
                <p>3-20 characters. Letters, numbers, and underscores only.</p>
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
                  'Create Username'
                )}
              </motion.button>
            </motion.div>
          </motion.form>
        </motion.div>
      </Card>
    </div>
  );
}