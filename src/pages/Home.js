import React, { useState, useEffect } from 'react';
import { Row, Col, Container, Alert, Button, Nav, Spinner } from 'react-bootstrap';
import ImageCard from '../components/ImageCard';
import ImageService from '../services/ImageService';
import { Link } from 'react-router-dom';


const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
  if (!base64String) return null;
  return `data:${mimeType};base64,${base64String}`;
};

const Home = () => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchImages(); // Fetch all images on initial load
  }, []);

  const fetchImages = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await ImageService.getAllImages();
      setImages(response.data || []); // Ensure images is an array
    } catch (err) {
      console.error('Error fetching images:', err);
      setError('Failed to load images. Please try again later.');
      setImages([]); // Reset images on error
    } finally {
      setLoading(false);
    }
  };


  return (
      <>
        <div className="hero-section" style={{ backgroundImage:'url(/images/bg.webp)', backgroundSize: 'cover', backgroundPosition: 'center'}}>
          <Container>
            <h1 className="hero-title">Find the perfect image for your project</h1>
            <p className="hero-subtitle">Discover and purchase high-quality images from talented photographers</p>
            <Button as={Link} to="/search" variant="primary" size="lg" className="px-4 py-2">
              Explore the Collection
            </Button>
          </Container>
        </div>

        <Container className="my-5">
          <h2 className="mb-4 text-center">Fresh finds for your creative projects</h2>

          {loading ? (
              <div className="text-center my-5">
                <Spinner animation="border" variant="primary" role="status">
                  <span className="visually-hidden">Loading...</span>
                </Spinner>
              </div>
          ) : error ? (
              <Alert variant="danger">{error}</Alert>
          ) : images.length === 0 ? (
              <div className="text-center my-5">
                <Alert variant="info">No images available at the moment.</Alert>
              </div>
          ) : (
              <Row xs={1} sm={2} md={3} lg={4} className="g-4">
                {images.map(image => (
                    <Col key={image.imageId}>
                      <ImageCard image={image} />
                    </Col>
                ))}
              </Row>
          )}
          <div className="text-center mt-5 mb-4">
            <h3>Ready to share your own images?</h3>
            <p className="text-muted mb-4">Join our community of photographers and start selling today</p>
            <Button as={Link} to="/upload" variant="primary" size="lg">
              Start Selling
            </Button>
          </div>
        </Container>


        <footer className="footer mt-5">
        </footer>
      </>
  );
};

export default Home;