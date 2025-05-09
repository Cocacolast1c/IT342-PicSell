import React, { useState } from 'react';
// Import Spinner for loading state
import { Container, Form, Button, Card, Alert, Row, Col, InputGroup, Spinner } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import ImageService from '../services/ImageService';
import AuthService from '../services/AuthService';

const readFileAsBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const base64String = reader.result?.split(',')[1];
      if (base64String) {
        resolve(base64String);
      } else {
        reject(new Error("Could not read file as Base64."));
      }
    };
    reader.onerror = (error) => reject(error);
    reader.readAsDataURL(file);
  });
};

const UploadImage = ({ currentUser }) => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    imageName: '',
    imageDescription: '',
    price: '',
    category: 'nature',
    licenseType: 'Standard'
  });

  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!currentUser || !currentUser.userId) {
    console.warn("No user logged in, redirecting to login.");
    navigate('/login');
    return null;
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setError(null);
    if (file && file.type.startsWith('image/')) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.onerror = () => {
        setError("Failed to read file preview.");
        setSelectedFile(null);
        setPreviewUrl(null);
      }
      reader.readAsDataURL(file);
    } else if (file) {
      setSelectedFile(null);
      setPreviewUrl(null);
      setError("Please select a valid image file (JPG, PNG, etc.).");
    } else {
      setSelectedFile(null);
      setPreviewUrl(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    if (!formData.imageName || !formData.imageDescription || !formData.price || !formData.category || !formData.licenseType) {
      setError('Please fill in all required fields');
      return;
    }
    if (!selectedFile) {
      setError('Please select an image to upload');
      return;
    }
    const price = parseFloat(formData.price);
    if (isNaN(price) || price <= 0) {
      setError('Please enter a valid positive price');
      return;
    }

    setLoading(true);

    try {
      const imageFileBase64 = await readFileAsBase64(selectedFile);

      const imageData = {
        imageName: formData.imageName,
        imageDescription: formData.imageDescription,
        price: price,
        tags: formData.category,
        licenseType: formData.licenseType,
        imageFile: imageFileBase64,
        uploader: { userId: currentUser.userId }

      };

      await ImageService.createImage(imageData);

      setSuccess(true);
      setFormData({
        imageName: '', imageDescription: '', price: '', category: 'nature', licenseType: 'Standard'
      });
      setSelectedFile(null);
      setPreviewUrl(null);

      setTimeout(() => { navigate('/profile'); }, 2000);

    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to upload image. Check console & backend logs.';
      setError(errorMessage);
      console.error('Upload error details:', err.response || err);
    } finally {
      setLoading(false);
    }
  };

  return (
      <Container className="my-5">
        {/* Breadcrumb */}
        <div className="mb-4">
          <Link to="/" className="text-decoration-none text-muted">Home</Link>
          {' > '}
          <Link to="/profile" className="text-decoration-none text-muted">Your Shop</Link>
          {' > '}
          <span>Add a new listing</span>
        </div>

        <Row className="justify-content-center">
          <Col lg={10}>
            <div className="text-center mb-4">
              <h1 className="fs-2 mb-2">Add a new listing</h1>
              <p className="text-muted">Share your work with the PicSell community</p>
            </div>

            {error && <Alert variant="danger" onClose={() => setError(null)} dismissible><i className="bi bi-exclamation-circle me-2"></i>{error}</Alert>}
            {success && <Alert variant="success"><i className="bi bi-check-circle me-2"></i>Image uploaded successfully! Redirecting...</Alert>}

            <Card className="border-0 shadow-sm mb-4">
              <Card.Body className="p-4">
                <Form onSubmit={handleSubmit}>
                  <Row>
                    <Col lg={6} className="mb-4 mb-lg-0">
                      <h3 className="fs-5 mb-3">Photo</h3>
                      <div className="upload-area p-4 border rounded text-center">
                        {previewUrl ? (
                            <div className="position-relative">
                              <img src={previewUrl} alt="Preview" className="img-fluid rounded mb-3" style={{ maxHeight: '300px' }} />
                              <Button variant="light" size="sm" className="position-absolute top-0 end-0 m-2 rounded-circle" style={{ width: '32px', height: '32px', padding: '0' }} onClick={() => { setSelectedFile(null); setPreviewUrl(null); }} aria-label="Remove image preview"> <i className="bi bi-x"></i> </Button>
                            </div>
                        ) : (
                            <>
                              <div className="mb-3"><i className="bi bi-cloud-arrow-up" style={{ fontSize: '3rem' }}></i></div>
                              <p className="mb-3">Drag and drop or click to upload</p>
                              <Form.Control type="file" accept="image/*" onChange={handleFileChange} required className="d-none" id="fileInput" aria-describedby="fileHelp"/>
                              <Button variant="outline-primary" onClick={() => document.getElementById('fileInput').click()}>Choose a file</Button>
                            </>
                        )}
                      </div>
                      <Form.Text id="fileHelp" className="text-muted d-block mt-2"><i className="bi bi-info-circle me-1"></i>Use high-quality images (JPG, PNG).</Form.Text>
                    </Col>

                    <Col lg={6}>
                      <h3 className="fs-5 mb-3">Listing details</h3>

                      <Form.Group className="mb-3" controlId="formImageName">
                        <Form.Label>Image Name</Form.Label>
                        <Form.Control type="text" name="imageName" value={formData.imageName} onChange={handleChange} placeholder="Enter image name" required className="py-2" aria-describedby="imageNameHelp"/>
                        <Form.Text id="imageNameHelp" className="text-muted">Include keywords buyers might search for.</Form.Text>
                      </Form.Group>

                      <Form.Group className="mb-3" controlId="formCategory">
                        <Form.Label>Category (used as Tag)</Form.Label>
                        <Form.Select name="category" value={formData.category} onChange={handleChange} className="py-2" required>
                          <option value="nature">Nature</option>
                          <option value="urban">Urban</option>
                          <option value="people">People</option>
                          <option value="abstract">Abstract</option>
                          <option value="other">Other</option>
                        </Form.Select>
                      </Form.Group>

                      <Form.Group className="mb-3" controlId="formLicenseType">
                        <Form.Label>License Type</Form.Label>
                        <Form.Select name="licenseType" value={formData.licenseType} onChange={handleChange} required className="py-2">
                          <option value="Standard">Standard</option>
                          <option value="Extended">Extended</option>
                          <option value="Editorial">Editorial</option>
                        </Form.Select>
                      </Form.Group>
                      <Form.Group className="mb-3" controlId="formImageDescription">
                        <Form.Label>Description</Form.Label>
                        <Form.Control as="textarea" rows={3} name="imageDescription" value={formData.imageDescription} onChange={handleChange} placeholder="Describe your image" required className="py-2"/>
                      </Form.Group>

                      <Form.Group className="mb-4" controlId="formPrice">
                        <Form.Label>Price ($)</Form.Label>
                        <InputGroup>
                          <InputGroup.Text>$</InputGroup.Text>
                          <Form.Control type="number" name="price" value={formData.price} onChange={handleChange} placeholder="19.99" step="0.01" min="0.01" required className="py-2"/>
                        </InputGroup>
                      </Form.Group>
                    </Col>
                  </Row>

                  <hr className="my-4" />

                  <div className="d-flex justify-content-between">
                    <Button variant="outline-secondary" onClick={() => navigate('/profile')} disabled={loading}>Cancel</Button>
                    <Button variant="primary" type="submit" disabled={loading} className="px-4">
                      {loading ? (
                          <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2"/>Uploading...</>
                      ) : 'Publish listing'}
                    </Button>
                  </div>
                </Form>
              </Card.Body>
            </Card>

            <div className="text-center">
              <p className="text-muted small">By publishing, you agree to PicSell's <Link to="/terms" className="text-decoration-none">Terms of Service</Link> and <Link to="/privacy" className="text-decoration-none">Seller Policy</Link>.</p>
            </div>
          </Col>
        </Row>
      </Container>
  );
};

export default UploadImage;