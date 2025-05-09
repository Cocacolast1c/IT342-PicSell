import React, { useState } from 'react';
import { Form, Button, Container, Row, Col, Card, Alert, InputGroup } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Login = ({ setCurrentUser }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }

    setLoading(true);

    try {
      const user = await AuthService.login(username, password);
      setCurrentUser(user);
      navigate('/');
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to login. Please check your credentials.';
      setError(errorMessage);
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };


  const handleGoogleLogin = () => {
    AuthService.redirectToGoogleLogin();
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col md={6} lg={5}>
            <div className="text-center mb-4">
              <h1 className="fs-2 mb-1">Sign in to PicSell</h1>
              <p className="text-muted">Welcome back! Please enter your details</p>
            </div>

            {error && (
                <Alert variant="danger" className="mb-4">
                  <i className="bi bi-exclamation-circle me-2"></i>
                  {error}
                </Alert>
            )}

            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4">
                <Form onSubmit={handleLogin}>
                  <Form.Group className="mb-3" controlId="formUsername">
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Enter your username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="py-2"
                        required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4" controlId="formPassword">
                    <div className="d-flex justify-content-between">
                      <Form.Label>Password</Form.Label>
                      {/* TBC: Link to a password reset page*/}
                      {/* <Link to="/forgot-password" className="text-decoration-none small">Forgot password?</Link> */}
                    </div>
                    <InputGroup>
                      <Form.Control
                          type={showPassword ? "text" : "password"}
                          placeholder="Enter your password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="py-2"
                          required
                      />
                      <Button
                          variant="outline-secondary"
                          onClick={togglePasswordVisibility}
                          className="border-start-0"
                      >
                        <i className={`bi bi-eye${showPassword ? '-slash' : ''}`}></i>
                      </Button>
                    </InputGroup>
                  </Form.Group>

                  <Button
                      variant="primary"
                      type="submit"
                      className="w-100 py-2 mb-3"
                      disabled={loading}
                  >
                    {loading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                          Signing in...
                        </>
                    ) : 'Sign in'}
                  </Button>

                  <div className="text-center my-3">
                    <span className="text-muted small">OR</span>
                  </div>
                  <Button
                      variant="outline-danger"
                      onClick={handleGoogleLogin}
                      className="w-100 py-2 mb-3"
                      disabled={loading}
                  >
                    <i className="bi bi-google me-2"></i> Sign in with Google
                  </Button>


                  <div className="text-center">
                    <p className="mb-0">Don't have an account? <Link to="/register" className="text-decoration-none fw-bold">Register</Link></p>
                  </div>
                </Form>
              </Card.Body>
            </Card>

            <div className="text-center mt-4">
              <p className="text-muted small">By signing in, you agree to PicSell's <Link to="/" className="text-decoration-none">Terms of Use</Link> and <Link to="/" className="text-decoration-none">Privacy Policy</Link>.</p>
            </div>
          </Col>
        </Row>
      </Container>
  );
};

export default Login;