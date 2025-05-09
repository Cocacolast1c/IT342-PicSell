import React, { useState, useEffect } from 'react'; // Added useEffect
import { Form, Button, Alert, Container, Card, Row, Col, InputGroup, ListGroup } from 'react-bootstrap'; // Added ListGroup
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  //Password Validator
  const [passwordCriteria, setPasswordCriteria] = useState({
    minLength: false,   // At least 8 characters
    uppercase: false,   // At least one uppercase letter
    lowercase: false,   // At least one lowercase letter
    number: false,      // At least one number
    specialChar: false, // At least one special character
  });
  const [isPasswordValid, setIsPasswordValid] = useState(false);
  const [passwordsMatch, setPasswordsMatch] = useState(true);
  const [passwordFocused, setPasswordFocused] = useState(false);


  const navigate = useNavigate();

  //Validation Logic
  const validatePassword = (password) => {
    const minLength = password.length >= 8;
    const uppercase = /[A-Z]/.test(password);
    const lowercase = /[a-z]/.test(password);
    const number = /[0-9]/.test(password);
    const specialChar = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]/.test(password);
    const newCriteria = { minLength, uppercase, lowercase, number, specialChar };
    setPasswordCriteria(newCriteria);
    const allValid = Object.values(newCriteria).every(Boolean);
    setIsPasswordValid(allValid);
    return newCriteria;
  };

  useEffect(() => {
    if (formData.password || passwordFocused) {
      validatePassword(formData.password);
    }
    setPasswordsMatch(formData.password === formData.confirmPassword || formData.confirmPassword === '');
  }, [formData.password, formData.confirmPassword, passwordFocused]);


  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (error) setError('');
  };

  // --- ADDED: Focus/Blur Handlers ---
  const handlePasswordFocus = () => { setPasswordFocused(true); };
  const handlePasswordBlur = () => {setPasswordFocused(!!formData.password); };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const { username, email, password, confirmPassword } = formData;

    // 1. Basic non-empty checks
    if (!username || !email || !password || !confirmPassword) {
      setError('Please fill in all required fields');
      return;
    }

    // 2. Email format check
    if (!validateEmail(email)) {
      setError('Please enter a valid email address');
      return;
    }

    // 3. Password criteria check
    const checkCriteria = {
      minLength: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password),
      specialChar: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]/.test(password)
    };
    const checkPasswordIsValid = Object.values(checkCriteria).every(Boolean);

    console.log(`Password: "${password}"`);
    console.log("Validation Check Results:", { checkPasswordIsValid, criteriaMet: checkCriteria });

    if (!checkPasswordIsValid) {
      setError('Password does not meet all the requirements.');
      console.log("%cSubmit blocked: Password criteria not met.", "color:red; font-weight:bold;");
      setPasswordCriteria(checkCriteria);
      setIsPasswordValid(false);
      return;
    }

    // 4. Password match check
    const checkPasswordsMatch = password === confirmPassword;
    console.log(`Passwords Match Check: ${checkPasswordsMatch}`);

    if (!checkPasswordsMatch) {
      setError('Passwords do not match');
      console.log("%cSubmit blocked: Passwords do not match.", "color:red; font-weight:bold;");

      setPasswordsMatch(false);
      setPasswordCriteria(checkCriteria);
      setIsPasswordValid(true);
      return;
    }

    // 5. All checks passed
    console.log("%cSubmit Proceeding: All checks passed.", "color:green; font-weight:bold;");
    setLoading(true);
    try {
      await AuthService.register(username, email, password); // Use validated values
      setSuccess('Registration successful! You can now login.');
      setTimeout(() => { navigate('/login'); }, 2000);
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to register.';
      setError(errorMessage);
      console.error('Registration error:', err);
    } finally {
      setLoading(false);
    }
  };


  const validateEmail = (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
  };

  const togglePasswordVisibility = () => { setShowPassword(!showPassword); };
  const toggleConfirmPasswordVisibility = () => { setShowConfirmPassword(!showConfirmPassword); };

  const renderValidationItem = (isValid, text) => (
      <ListGroup.Item className={`border-0 p-1 small d-flex align-items-center ${isValid ? 'text-success' : 'text-danger'}`}>
        <i className={`bi ${isValid ? 'bi-check-circle-fill' : 'bi-x-circle-fill'} me-2`}></i>
        {text}
      </ListGroup.Item>
  );

  const isRegisterDisabled = loading || !formData.username || !formData.email || !formData.password || !formData.confirmPassword || !isPasswordValid || !passwordsMatch;


  return (
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col md={6} lg={5}>
            <div className="text-center mb-4">
              <h1 className="fs-2 mb-1">Create your account</h1>
              <p className="text-muted">Join the PicSell community today</p>
            </div>

            {error && <Alert variant="danger" className="mb-4"><i className="bi bi-exclamation-circle me-2"></i>{error}</Alert>}
            {success && <Alert variant="success" className="mb-4"><i className="bi bi-check-circle me-2"></i>{success}</Alert>}

            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4">
                <Form onSubmit={handleRegister} noValidate>
                  <Row>
                    <Col xs={12}>
                      <Form.Group className="mb-3" controlId="formUsername">
                        <Form.Label>Username</Form.Label>
                        <Form.Control type="text" placeholder="Choose a username" name="username" value={formData.username} onChange={handleChange} className="py-2" required/>
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-3" controlId="formEmail">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" placeholder="Enter your email" name="email" value={formData.email} onChange={handleChange} className="py-2" required/>
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formPassword">
                    <Form.Label>Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                          type={showPassword ? "text" : "password"}
                          placeholder="Create a password"
                          name="password"
                          value={formData.password}
                          onChange={handleChange}
                          onFocus={handlePasswordFocus}
                          onBlur={handlePasswordBlur}
                          isInvalid={ (passwordFocused || formData.password) && !isPasswordValid }
                          className="py-2"
                          required
                      />
                      <Button variant="outline-secondary" onClick={togglePasswordVisibility} className="border-start-0"> <i className={`bi bi-eye${showPassword ? '-slash' : ''}`}></i> </Button>
                    </InputGroup>
                    {(passwordFocused || formData.password) && (
                        <ListGroup variant="flush" className="mt-2 password-criteria-list">
                          {renderValidationItem(passwordCriteria.minLength, 'At least 8 characters')}
                          {renderValidationItem(passwordCriteria.uppercase, 'At least one uppercase letter (A-Z)')}
                          {renderValidationItem(passwordCriteria.lowercase, 'At least one lowercase letter (a-z)')}
                          {renderValidationItem(passwordCriteria.number, 'At least one number (0-9)')}
                          {renderValidationItem(passwordCriteria.specialChar, 'At least one special character (!@#$...etc)')}
                        </ListGroup>
                    )}
                  </Form.Group>

                  <Form.Group className="mb-4" controlId="formConfirmPassword">
                    <Form.Label>Confirm Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                          type={showConfirmPassword ? "text" : "password"}
                          placeholder="Confirm your password"
                          name="confirmPassword"
                          value={formData.confirmPassword}
                          onChange={handleChange}
                          isInvalid={formData.confirmPassword && !passwordsMatch}
                          className="py-2"
                          required
                      />
                      <Button variant="outline-secondary" onClick={toggleConfirmPasswordVisibility} className="border-start-0"> <i className={`bi bi-eye${showConfirmPassword ? '-slash' : ''}`}></i> </Button>
                      <Form.Control.Feedback type="invalid">
                        Passwords do not match.
                      </Form.Control.Feedback>
                    </InputGroup>
                  </Form.Group>

                  <Button
                      variant="primary"
                      type="submit"
                      className="w-100 py-2 mb-3"
                      disabled={isRegisterDisabled}
                  >
                    {loading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                          Creating account...
                        </>
                    ) : 'Register'}
                  </Button>

                  <div className="text-center">
                    <p className="mb-0"> Already have an account? <Link to="/login" className="text-decoration-none fw-bold">Sign in</Link> </p>
                  </div>
                </Form>
              </Card.Body>
            </Card>
            <div className="text-center mt-4">
              <p className="text-muted small"> By registering, you agree to PicSell's <Link to="/" className="text-decoration-none">Terms of Use</Link> and <Link to="/" className="text-decoration-none">Privacy Policy</Link>. </p>
            </div>
          </Col>
        </Row>
      </Container>
  );
};

const renderValidationItem = (isValid, text) => (
    <ListGroup.Item className={`border-0 p-1 small d-flex align-items-center ${isValid ? 'text-success' : 'text-danger'}`}>
      <i className={`bi ${isValid ? 'bi-check-circle-fill' : 'bi-x-circle-fill'} me-2`}></i>
      {text}
    </ListGroup.Item>
);

export default Register;