import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

interface User {
  id: string;
  email: string;
  username?: string;
  name?: string;
  avatarUrl?: string;
  isNewUser?: boolean;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  loginWithGoogle: () => void;
  logout: () => void;
  setUsername: (username: string) => Promise<void>;
  checkUserStatus: () => void;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const checkUserStatus = async () => {
    try {
      const res = await axios.get('/api/auth/session');
      const userData = res.data.user;
      
      setUser(userData);
      
      // Check if user needs to complete profile (no username)
      // if (userData && !userData.username) {
      //   window.location.href = '/complete-profile';
      // }
    } catch (err) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkUserStatus();
  }, []);

  const loginWithGoogle = () => {
    // Force Google to show account selection screen
    window.location.href = '/api/auth/google?prompt=select_account';
  };
  
  const logout = async () => {
    try {
      console.log("Logout initiated");
      
      // Clear all cookies by setting expiration in the past
      document.cookie.split(";").forEach(cookie => {
        const eqPos = cookie.indexOf("=");
        const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim();
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
      });
      
      // Clear local and session storage
      localStorage.clear();
      sessionStorage.clear();
      
      // Call backend logout endpoint
      await axios.post('/api/auth/logout');
      console.log("Logout successful");
      
      // Clear React state
      setUser(null);
      
      // Optional: Redirect to Google's logout URL before returning to your app
      // This is the most thorough approach but requires additional backend configuration
      const googleLogoutUrl = "https://accounts.google.com/logout";
      const returnTo = encodeURIComponent("http://localhost:5173/login");
      
      // Option 1: Just redirect to your login page
      window.location.href = '/login';
      
      // Option 2: For complete Google logout (uncomment if needed)
      // window.location.href = googleLogoutUrl + "?continue=" + returnTo;
    } catch (error) {
      console.error("Logout failed:", error);
      setUser(null);
      window.location.href = '/login';
    }
  };

  const setUsername = async (username: string) => {
    await axios.post('/api/profile/username', { username });
    setUser(prev => prev ? { ...prev, username } : prev);
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      loginWithGoogle, 
      logout, 
      setUsername,
      checkUserStatus 
    }}>
      {children}
    </AuthContext.Provider>
  );
};