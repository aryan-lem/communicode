// src/components/Card.tsx
import { ReactNode } from 'react';
import { motion } from 'framer-motion';
import '../styles.css';

export const Card = ({ children }: { children: ReactNode }) => (
  <motion.div
    initial={{ opacity: 0, y: 30, scale: 0.95 }}
    animate={{ opacity: 1, y: 0, scale: 1 }}
    transition={{ 
      duration: 0.5, 
      ease: [0.22, 1, 0.36, 1]  // Custom ease curve for smooth animation
    }}
    className="modern-card"
  >
    <div className="card-backdrop"></div>
    <div className="card-content">
      {children}
    </div>
  </motion.div>
);