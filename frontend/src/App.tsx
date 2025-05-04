// src/App.tsx
import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage.tsx';
import CompleteProfilePage from './pages/CompleteProfilePage';
import CreateRoomPage from './pages/CreateRoomPage';
import RoomPage from './pages/RoomPage.tsx';
import HomePage from './pages/HomePage.tsx';

// A wrapper component to redirect based on auth state
const ProtectedRoute = ({ 
  children, 
  requireAuth = true,
  requireUsername = false
}: { 
  children: React.ReactElement, 
  requireAuth?: boolean,
  requireUsername?: boolean 
}) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }
  
  // If we require auth and there's no user, redirect to login
  if (requireAuth && !user) {
    return <Navigate to="/login" replace />;
  }
  
  // If the user needs a username but doesn't have one
  if (requireUsername && user && !user.username) {
    return <Navigate to="/complete-profile" replace />;
  }
  
  // If we're at login page but user is already logged in
  if (!requireAuth && user) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

const AppRoutes = () => {
  const { checkUserStatus, user, loading } = useAuth();
  const navigate = useNavigate();
  
  // Check user status when app loads and after Google redirect
  useEffect(() => {
    const checkAuth = async () => {
      // Get current URL path
      const path = window.location.pathname;
      
      // If this is a redirect from Google auth
      if (path.includes('/auth/callback')) {
        await checkUserStatus();
        if (user) {
          if (user.username) {
            navigate('/', { replace: true });
          } else {
            navigate('/complete-profile', { replace: true });
          }
        } else {
          navigate('/login?error=auth_failed', { replace: true });
        }
      }
    };
    
    checkAuth();
  }, []);
  
  return (
    <Routes>
      <Route path="/login" element={
        <ProtectedRoute requireAuth={false}>
          <LoginPage />
        </ProtectedRoute>
      } />
      
      <Route path="/complete-profile" element={
         <ProtectedRoute requireAuth={true} requireUsername={false}>
          <CompleteProfilePage />
        </ProtectedRoute>
      } />
      
      <Route path="/" element={
        <ProtectedRoute requireAuth={true} requireUsername={true}>
          <HomePage />
        </ProtectedRoute>
      } />
      
      <Route path="/create-room" element={
        <ProtectedRoute requireAuth={true} requireUsername={true}>
          <CreateRoomPage />
        </ProtectedRoute>
      } />
      
      <Route path="/room/:roomId" element={
        <ProtectedRoute requireAuth={true} requireUsername={true}>
          <RoomPage />
        </ProtectedRoute>
      } />
      
      {/* Add a route to catch Google Auth callbacks */}
      <Route path="/auth/callback" element={
        <div className="loading-screen">Processing login...</div>
      } />
      
      {/* Default redirect */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}