import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Spinner } from 'react-bootstrap'; // Import Spinner

import Navigation from './components/Navigation';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ImageDetail from './pages/ImageDetail';
import Profile from './pages/Profile';
import UploadImage from './pages/UploadImage';
import Transactions from './pages/Transactions';
import Search from './pages/Search';
import AuthService from './services/AuthService';
import AuthCallback from './pages/AuthCallback';
import PaymentSuccess from "./pages/PaymentSuccess";
import Inventory from "./pages/Inventory";
import AdminDashboard from "./pages/AdminDashboard";

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    }
    setLoading(false);
  }, []);

  const PrivateRoute = ({ children }) => {
    if (loading) return <div className="text-center mt-5"><Spinner animation="border" /></div>;
    return currentUser ? children : <Navigate to="/login" replace />;
  };

  const AdminRoute = ({ children }) => {
    if (loading) return <div className="text-center mt-5"><Spinner animation="border" /></div>;

    const isAdmin = currentUser && currentUser.roles === 'ROLE_ADMIN';

    if (!currentUser) {
      return <Navigate to="/login" replace />;
    }
    if (!isAdmin) {
      return <Navigate to="/" replace />;

    }

    return children;
  };


  return (
      <Router>
        <div className="App">
          <Navigation currentUser={currentUser} setCurrentUser={setCurrentUser} />
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login setCurrentUser={setCurrentUser} />} />
            <Route path="/register" element={<Register />} />
            <Route path="/images/:id" element={<ImageDetail currentUser={currentUser} />} />
            <Route path="/search" element={<Search />} />
            <Route path="/auth/google/callback" element={<AuthCallback setCurrentUser={setCurrentUser} />} />
            <Route path="/payment-success" element={<PaymentSuccess />} />

            {/* Authenticated User Routes */}
            <Route path="/profile" element={ <PrivateRoute> <Profile currentUser={currentUser} setCurrentUser={setCurrentUser} /> </PrivateRoute> } />
            <Route path="/upload" element={ <PrivateRoute> <UploadImage currentUser={currentUser} /> </PrivateRoute> } />
            <Route path="/transactions" element={ <PrivateRoute> <Transactions currentUser={currentUser} /> </PrivateRoute> } />
            <Route path="/inventory" element={ <PrivateRoute> <Inventory currentUser={currentUser} /> </PrivateRoute> } />

            {/*Admin Route */}
            <Route path="/admin" element={ <AdminRoute> <AdminDashboard /> </AdminRoute> } />

            {/* Catch-all Route */}
            <Route path="*" element={ <Navigate to="/" replace /> } />
          </Routes>
        </div>
      </Router>
  );
}

export default App;