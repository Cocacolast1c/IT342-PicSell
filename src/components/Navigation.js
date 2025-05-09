import React, { useState } from 'react';
import { Navbar, Nav, Container, Button, Form, InputGroup } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Navigation = ({ currentUser, setCurrentUser }) => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');

  const handleLogout = () => {
    AuthService.logout();
    setCurrentUser(null);
    navigate('/login');
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchTerm)}`);
      setSearchTerm('');
    }
  };

  // Check if the current user has admin role
  const isAdmin = currentUser && currentUser.roles === 'ROLE_ADMIN';

  return (
      <Navbar bg="white" expand="lg" className="shadow-sm main-navbar">
        <Container>
          <Navbar.Brand as={Link} to="/" className="navbar-brand-custom">PicSell</Navbar.Brand>

          <Form className="d-none d-md-flex mx-auto search-form-custom" style={{ width: '40%' }} onSubmit={handleSearch}>
            <InputGroup>
              <Form.Control
                  type="search"
                  placeholder="Search for images..."
                  aria-label="Search"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="search-bar"
              />
              <Button variant="primary" type="submit" className="search-button">
                <i className="bi bi-search me-1"></i> Search
              </Button>
            </InputGroup>
          </Form>

          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="ms-auto align-items-center nav-links-custom">
              <Nav.Link as={Link} to="/" className="nav-link-custom">Home</Nav.Link>
              {currentUser && (
                  <>
                    <Nav.Link as={Link} to="/upload" className="nav-link-custom">Sell</Nav.Link>
                    <Nav.Link as={Link} to="/transactions" className="nav-link-custom">Purchases & Sales</Nav.Link>
                    <Nav.Link as={Link} to="/inventory" className="nav-link-custom">Inventory</Nav.Link>
                  </>
              )}

              {isAdmin && (
                  <Nav.Link as={Link} to="/admin" className="nav-link-custom fw-bold">Admin</Nav.Link>
              )}

              {currentUser ? (
                  <>
                    <Nav.Link as={Link} to="/profile" className="nav-link-custom profile-link">
                      <i className="bi bi-person-circle me-1"></i> {currentUser.username || currentUser.email}
                    </Nav.Link>
                    <Button variant="outline-primary" onClick={handleLogout} className="ms-2 auth-button">
                      Sign out
                    </Button>
                  </>
              ) : (
                  <>
                    <Nav.Link as={Link} to="/login" className="nav-link-custom">Sign in</Nav.Link>
                    <Button as={Link} to="/register" variant="primary" className="ms-2 auth-button register-button">
                      Register
                    </Button>
                  </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
  );
};

export default Navigation;