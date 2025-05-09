import React, { useState, useEffect, useRef } from 'react';
import { Container, Row, Col, Card, Button, Form, Alert, Image } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import ImageService from '../services/ImageService';
import AuthService from '../services/AuthService';
import ImageCard from '../components/ImageCard';

const Profile = ({ currentUser, setCurrentUser }) => {
  const navigate = useNavigate();

  const [userImages, setUserImages] = useState([]);
  const [loadingListings, setLoadingListings] = useState(true);
  const [errorListings, setErrorListings] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({ email: '', username: '' });
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [profileUpdateLoading, setProfileUpdateLoading] = useState(false);
  const [updateSuccess, setUpdateSuccess] = useState(false);
  const [updateError, setUpdateError] = useState(null);
  const fileInputRef = useRef(null);

  const [currentProfileImageUrl, setCurrentProfileImageUrl] = useState(null);

  const placeholderImagePath = '/images/placeholder.jpg';

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }
    setFormData({ email: currentUser.email || '', username: currentUser.username || '' });


    if (currentUser.imagefile && typeof currentUser.imagefile === 'string' && currentUser.imagefile.length > 20) { // Basic check
      setCurrentProfileImageUrl(`data:image/jpeg;base64,${currentUser.imagefile}`);
    } else {
      setCurrentProfileImageUrl(placeholderImagePath);
    }
    fetchUserImages();
  }, [currentUser, navigate]);

  const fetchUserImages = async () => {
    if (!currentUser || !currentUser.userId) return;
    setLoadingListings(true);
    try {
      const response = await ImageService.getImagesByUserId(currentUser.userId);
      setUserImages(response.data || []);
    } catch (err) {
      setErrorListings('Failed to load your images. Please try again later.');
      console.error('Error fetching user images:', err);
      setUserImages([]);
    } finally { setLoadingListings(false); }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith('image/')) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => { setPreviewUrl(reader.result); };
      reader.readAsDataURL(file);
    } else { setSelectedFile(null); setPreviewUrl(null); }
  };

  const readFileAsBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => { const base64String = reader.result.split(',')[1]; resolve(base64String); };
      reader.onerror = (error) => reject(error);
      reader.readAsDataURL(file);
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setUpdateSuccess(false); setUpdateError(null); setProfileUpdateLoading(true);
    try {
      let base64Image = null; if (selectedFile) { base64Image = await readFileAsBase64(selectedFile); }
      const updatedDetails = { username: formData.username, email: formData.email, ...(base64Image && { imagefile: base64Image }) };
      await AuthService.updateUser(currentUser.userId, updatedDetails);
      const updatedUser = AuthService.getCurrentUser();
      setCurrentUser(updatedUser);

      if (updatedUser.imagefile && typeof updatedUser.imagefile === 'string' && updatedUser.imagefile.length > 20) {
        setCurrentProfileImageUrl(`data:image/jpeg;base64,${updatedUser.imagefile}`);
      } else {
        setCurrentProfileImageUrl(placeholderImagePath);
      }

      setUpdateSuccess(true); setEditMode(false); setSelectedFile(null); setPreviewUrl(null);
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to update profile.';
      setUpdateError(errorMessage); console.error('Profile update error:', err);
    } finally { setProfileUpdateLoading(false); }
  };

  if (!currentUser) { return null; }

  const imageSrcToDisplay = previewUrl || currentProfileImageUrl || placeholderImagePath;

  return (
      <Container className="my-5">
        <Row>
          {/* Profile Info Column */}
          <Col lg={4}>
            <Card className="mb-4">
              <Card.Body className="text-center">
                <Image
                    src={imageSrcToDisplay}
                    roundedCircle
                    style={{ width: '100px', height: '100px', objectFit: 'cover', marginBottom: '1rem', border: '2px solid #eee' }}
                    alt={`${currentUser.username}'s profile`}
                    onError={(e) => { if (e.target.src !== placeholderImagePath) { e.target.onerror = null; e.target.src = placeholderImagePath;} }}
                />

                <h5 className="mb-3">@{currentUser.username}</h5>
                <p className="text-muted mb-4">{currentUser.email}</p>
                <div className="d-flex justify-content-center mb-2">
                  <Button variant={editMode ? "secondary" : "primary"} onClick={() => { setEditMode(!editMode); if(editMode) { setFormData({ email: currentUser.email || '', username: currentUser.username || '' }); setSelectedFile(null); setPreviewUrl(null); setUpdateError(null); setUpdateSuccess(false); } }}>
                    {editMode ? "Cancel" : "Edit Profile"}
                  </Button>
                </div>
              </Card.Body>
            </Card>

            {/* Edit Form Card */}
            {editMode && (
                <Card className="mb-4">
                  <Card.Body>
                    <Card.Title>Edit Profile</Card.Title>
                    {updateSuccess && <Alert variant="success">Profile updated!</Alert>}
                    {updateError && <Alert variant="danger">{updateError}</Alert>}
                    <Form onSubmit={handleSubmit}>
                      <Form.Group className="mb-3" controlId="formProfilePic">
                        <Form.Label>Profile Picture</Form.Label>
                        <Form.Control type="file" accept="image/*" onChange={handleFileChange} />
                        {previewUrl && ( <Image src={previewUrl} thumbnail style={{ maxHeight: '100px', marginTop: '10px' }} /> )}
                      </Form.Group>
                      <Form.Group className="mb-3" controlId="formUsername"><Form.Label>Username</Form.Label><Form.Control type="text" name="username" value={formData.username} onChange={handleChange} required /></Form.Group>
                      <Form.Group className="mb-3" controlId="formEmail"><Form.Label>Email</Form.Label><Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required /></Form.Group>
                      <Button variant="primary" type="submit" className="w-100" disabled={profileUpdateLoading}> {profileUpdateLoading ? 'Saving...' : 'Save Changes'} </Button>
                    </Form>
                  </Card.Body>
                </Card>
            )}
          </Col>

          {/* User's Image Listings Column*/}
          <Col lg={8}>
            <Card className="mb-4">
              <Card.Body>
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <Card.Title className="mb-0">My Image Listings</Card.Title>
                  <Button variant="outline-primary" size="sm" onClick={() => navigate('/upload')}> <i className="bi bi-plus-lg me-1"></i> Upload New Image </Button>
                </div>
                {/* --- Listing display logic --- */}
                {loadingListings ? ( <div className="text-center my-4"><div className="spinner-border text-primary"></div></div> )
                    : errorListings ? ( <Alert variant="danger">{errorListings}</Alert> )
                        : userImages.length === 0 ? ( <div className="text-center my-4"><p>You haven't uploaded any images yet.</p><Button variant="primary" onClick={() => navigate('/upload')}>Upload First</Button></div> )
                            : ( <Row xs={1} md={2} className="g-4 mt-2"> {userImages.map(image => ( <Col key={image.imageId}> <ImageCard image={image} /> </Col> ))} </Row> )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
  );
};

export default Profile;