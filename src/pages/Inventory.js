import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Alert, Spinner, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import InventoryService from '../services/InventoryService';
import ImageCard from '../components/ImageCard';

const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
    if (!base64String) return `https://via.placeholder.com/300?text=No+Image+Data`;
    const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
    if (typeof base64String === 'string' && base64Regex.test(base64String)) {
        return `data:${mimeType};base64,${base64String}`;
    }
    console.warn("Invalid Base64 string received for inventory image, using placeholder.");
    return 'https://via.placeholder.com/300?text=Invalid+Data';
};


const handleDownload = (imageFile, imageName) => {
    if (!imageFile || !imageName) {
        console.error("Missing image file data or name for download.");
        return;
    }
    try {
        const link = document.createElement('a');
        link.href = createDataUrl(imageFile);


        const extension = imageName.includes('.') ? imageName.split('.').pop() : 'jpg';
        link.download = `${imageName.replace(/.[^/.]+$/, "") || 'download'}.${extension}`;

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (error) {
        console.error("Error initiating download:", error);
        alert("Could not initiate download.");
    }
};


const Inventory = ({ currentUser }) => {
    const navigate = useNavigate();
    const [inventoryItems, setInventoryItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!currentUser) {
            navigate('/login');
            return;
        }
        fetchInventory();
    }, [currentUser]);

    const fetchInventory = async () => {
        if (!currentUser || !currentUser.userId) {
            setError("Cannot fetch inventory: User not logged in.");
            setLoading(false);
            return;
        };

        setLoading(true);
        setError(null);
        setInventoryItems([]);

        try {
            const response = await InventoryService.getInventoryByUserId(currentUser.userId);
            console.log("Fetched inventory raw data:", response.data);
            setInventoryItems(response.data || []);
        } catch (err) {
            const errorMsg = err.response?.data?.message || err.message || 'Failed to load your inventory.';
            setError(errorMsg);
            console.error('Error fetching inventory:', err.response || err);
            setInventoryItems([]);
        } finally {
            setLoading(false);
        }
    };


    return (
        <Container className="my-5">
            <div className="mb-4">
                <Link to="/" className="text-decoration-none text-muted">Home</Link>
                {' > '} <span>Your Inventory</span>
            </div>

            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="fs-2 mb-0">Your Purchased Images</h1>
            </div>

            {loading ? (
                <div className="text-center my-5"> <Spinner animation="border" variant="primary" /> </div>
            ) : error ? (
                <Alert variant="danger"><i className="bi bi-exclamation-circle me-2"></i>{error}</Alert>
            ) : inventoryItems.length === 0 ? (
                <div className="text-center py-5">
                    <div className="mb-4"><i className="bi bi-images" style={{ fontSize: '3rem', color: 'var(--etsy-gray)' }}></i></div>
                    <h3 className="fs-5 mb-3">Your inventory is empty</h3>
                    <p className="text-muted mb-4">Purchase images to add them to your collection.</p>
                    <Button as={Link} to="/" variant="primary">Browse Images</Button>
                </div>
            ) : (
                <Row xs={1} sm={2} md={3} lg={4} className="g-4">
                    {inventoryItems.map(item => (
                        item.image ? (
                            <Col key={item.inventoryId || item.image.imageId}>
                                <ImageCard image={item.image} />

                                <Button
                                    variant="outline-secondary"
                                    size="sm"
                                    className="mt-2 w-100"
                                    onClick={() => handleDownload(item.image.imageFile, item.image.imageName)}
                                    disabled={!item.image.imageFile}
                                >
                                    <i className="bi bi-download me-1"></i> Download
                                </Button>

                            </Col>
                        ) : (
                            <Col key={item.inventoryId || `missing-${Math.random()}`}>
                                <Alert variant="warning" className="h-100 d-flex align-items-center justify-content-center">Inventory item #{item.inventoryId} is missing image data.</Alert>
                            </Col>
                        )
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default Inventory;