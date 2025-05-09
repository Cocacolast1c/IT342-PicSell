import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Alert, Modal, Badge, Tabs, Tab, Spinner } from 'react-bootstrap';
import ImageService from '../services/ImageService';
import TransactionService from '../services/TransactionService';
import InventoryService from '../services/InventoryService';



const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
  if (!base64String) return `https://via.placeholder.com/800x600?text=No+Image+Data`;
  const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
  if (typeof base64String === 'string' && base64Regex.test(base64String)) {
    return `data:${mimeType};base64,${base64String}`;
  }
  console.warn("Invalid Base64 string received for image, using placeholder.");
  return `https://via.placeholder.com/800x600?text=Invalid+Data`;
};

const ImageDetail = ({ currentUser }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const numericId = parseInt(id, 10);

  // State variables
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [purchaseLoading, setPurchaseLoading] = useState(false);
  const [purchaseError, setPurchaseError] = useState(null);
  const [purchaseSuccess, setPurchaseSuccess] = useState(false);
  const [paymentApprovalUrl, setPaymentApprovalUrl] = useState(null);
  const [pendingTransactionId, setPendingTransactionId] = useState(null);


  const [inventory, setInventory] = useState([]);
  const [loadingInventory, setLoadingInventory] = useState(false);
  const [userOwnsImage, setUserOwnsImage] = useState(false);



  useEffect(() => {
    fetchImageDetails();
    if (currentUser && currentUser.userId) {
      fetchUserInventory();
    } else {
      setUserOwnsImage(false);
    }
  }, [id, currentUser]);

  useEffect(() => {
    if (!currentUser || !image || !inventory || inventory.length === 0) {
      setUserOwnsImage(false);
      return;
    }

    const owns = inventory.some(item => item?.image?.imageId === numericId);
    setUserOwnsImage(owns);
  }, [inventory, image, currentUser, numericId]);

  const fetchImageDetails = async () => {
    setLoading(true);
    setError(null);
    setImage(null);
    try {
      const response = await ImageService.getImageById(id);
      if (response.data) {
        setImage(response.data);
      } else {
        setError(`Image with ID ${id} not found.`);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to load image details.';
      setError(errorMessage);
      console.error(`Error fetching image details for ID ${id}:`, err);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserInventory = async () => {
    if (!currentUser || !currentUser.userId) return;

    setLoadingInventory(true);
    setUserOwnsImage(false);
    try {
      const response = await InventoryService.getInventoryByUserId(currentUser.userId);
      setInventory(response.data || []);
      console.log("Fetched user inventory:", response.data);
    } catch (err) {
      console.error("Failed to fetch user inventory:", err);
      setError("Could not verify image ownership. Please try refreshing."); // Inform user
      setInventory([]);
    } finally {
      setLoadingInventory(false);
    }
  };


  const handlePurchase = async () => {
    if (!currentUser) { navigate('/login'); return; }
    if (!image || !image.uploader?.userId || !image.imageId || typeof image.price !== 'number') {
      setPurchaseError('Image data is incomplete. Cannot proceed.');
      return;
    }
    if (userOwnsImage) {
      setPurchaseError("You already own this image.");
      setShowPurchaseModal(false);
      return;
    }

    setPurchaseLoading(true);
    setPurchaseError(null);
    setPurchaseSuccess(false);
    setPaymentApprovalUrl(null);
    setPendingTransactionId(null);
    let createdTransactionId = null;

    try {
      // Step 1: Create internal transaction record (Backend performs ownership check here too)
      const createResponse = await TransactionService.createTransaction(
          currentUser.userId, image.uploader.userId, image.imageId
      );

      if (!createResponse.data || !createResponse.data.transactionId) {
        throw new Error("Failed to create transaction record or get its ID.");
      }
      createdTransactionId = createResponse.data.transactionId;
      setPendingTransactionId(createdTransactionId);

      // Step 2: Initiate PayPal payment
      const paymentResponse = await TransactionService.makePayment(image.price, createdTransactionId);

      if (paymentResponse.data && paymentResponse.data.approvalUrl) {
        setPaymentApprovalUrl(paymentResponse.data.approvalUrl);
        setPurchaseSuccess(true); // Ready for PayPal redirection
      } else {
        throw new Error('Could not get payment approval URL.');
      }

    } catch (err) {
      if (err.response && err.response.status === 409) {
        setPurchaseError(err.response.data.message || "You already own this image.");
        setUserOwnsImage(true);
        setShowPurchaseModal(false);
      } else {
        const errorMessage = err.response?.data?.message || err.message || 'Failed to process purchase.';
        setPurchaseError(errorMessage);
        setPurchaseSuccess(false);
        setPaymentApprovalUrl(null);
        setPendingTransactionId(null);
      }
      console.error('Purchase process error:', err.response || err);
    } finally {
      setPurchaseLoading(false);
    }
  };


  const handleClosePurchaseModal = async () => {
    if (pendingTransactionId && purchaseSuccess && !purchaseError) {
      console.log(`User closing modal for pending transaction ID: ${pendingTransactionId}. Attempting cancellation.`);
      try {
        await TransactionService.updateSaleState(pendingTransactionId, 'CANCELLED');
        console.log(`Transaction ID: ${pendingTransactionId} marked as CANCELLED.`);
      } catch (cancelError) {
        console.error(`Could not mark transaction ${pendingTransactionId} as CANCELLED via API:`, cancelError.response || cancelError);
      }
    } else {
      console.log("Closing modal without cancellation call.");
    }
    setShowPurchaseModal(false);
    setPurchaseError(null);
    setPurchaseSuccess(false);
    setPaymentApprovalUrl(null);
    setPurchaseLoading(false);
    setPendingTransactionId(null);
  };


  if (loading) { return ( <Container className="text-center my-5"><Spinner animation="border" variant="primary" /></Container> ); }
  if (error || !image) { return ( <Container className="my-5"><Alert variant="danger">{error || 'Image data could not be loaded or was not found.'}</Alert><Button variant="primary" onClick={() => navigate('/')}>Back to Home</Button></Container> ); }

  const isOwnListing = currentUser && image.uploader && currentUser.userId === image.uploader.userId;
  const mainImageUrl = createDataUrl(image.imageFile);
  const modalImageUrl = createDataUrl(image.imageFile, 'image/jpeg'); // Use consistent helper


  let buyButton;
  if (isOwnListing) {
    buyButton = <Button variant="secondary" size="lg" className="w-100 py-3 mb-3" disabled>Your Listing</Button>;
  } else if (loadingInventory) {
    buyButton = <Button variant="primary" size="lg" className="w-100 py-3 mb-3" disabled><Spinner as="span" size="sm" role="status" aria-hidden="true" /> Checking ownership...</Button>;
  } else if (userOwnsImage) {
    buyButton = <Button as={Link} to="/inventory" variant="success" size="lg" className="w-100 py-3 mb-3"><i className="bi bi-bag-check-fill me-2"></i>View in Inventory</Button>;
  } else {
    buyButton = <Button variant="primary" size="lg" className="w-100 py-3 mb-3" onClick={() => { setPurchaseError(null); setPurchaseSuccess(false); setPaymentApprovalUrl(null); setPendingTransactionId(null); setShowPurchaseModal(true); }}> Buy now </Button>;
  }

  return (
      <>
        <Container className="my-5">
          <div className="mb-4">
            <Link to="/" className="text-decoration-none text-muted">Home</Link>
            {' > '} <span className="text-muted">Images</span>
            {' > '} <span>{image.imageName || 'Image Detail'}</span>
          </div>

          <Row className="gx-5">
            <Col lg={7}>
              <div className="position-relative mb-4">
                <img src={mainImageUrl} alt={image.imageName || 'Image'} className="img-fluid rounded image-detail-img w-100"/>
                {image.imageId && image.imageId % 3 === 0 && ( <Badge bg="warning" className="position-absolute top-0 start-0 m-3 px-2 py-1"> Bestseller </Badge> )}
              </div>
              <Tabs defaultActiveKey="description" className="mb-4">
                <Tab eventKey="description" title="Description"> <div className="p-3"> <p>{image.imageDescription || 'No description available.'}</p> </div> </Tab>
                <Tab eventKey="details" title="Details"> <div className="p-3"> <p><strong>Tags:</strong> {image.tags || 'None'}</p> <p><strong>License:</strong> {image.licenseType || 'Standard'}</p> </div> </Tab>
                <Tab eventKey="reviews" title="Reviews"> <div className="p-3"><p>No reviews yet.</p></div> </Tab>
              </Tabs>
            </Col>

            <Col lg={5}>
              {/* Purchase Card */}
              <Card className="border-0 shadow-sm">
                <Card.Body className="p-4">
                  <h1 className="fs-3 mb-2">{image.imageName || 'Untitled Image'}</h1>
                  {/* Uploader Info */}
                  <div className="d-flex align-items-center mb-3">
                    <div className="rounded-circle bg-secondary text-white d-flex align-items-center justify-content-center me-2" style={{ width: '30px', height: '30px', fontSize: '0.9rem' }}> {image.uploader?.username ? image.uploader.username.charAt(0).toUpperCase() : 'U'} </div>
                    <span> By <Link to={`/profile/${image.uploader?.userId || ''}`} className="text-decoration-none"> {image.uploader?.username || 'Unknown'} </Link> </span>
                  </div>
                  {/* Rating */}
                  <div className="d-flex align-items-center mb-3"> <span className="me-2">★★★★★</span> <span className="text-muted small">(5.0)</span> </div>
                  {/* Price */}
                  <h2 className="price fs-2 mb-4"> ${image.price?.toFixed(2) || '0.00'} </h2>
                  {/* Features */}
                  <div className="mb-4">
                    <div className="d-flex align-items-center mb-2"><i className="bi bi-check-circle-fill text-success me-2"></i><span>Digital Download</span></div>
                    <div className="d-flex align-items-center mb-2"><i className="bi bi-check-circle-fill text-success me-2"></i><span>High Resolution</span></div>
                    <div className="d-flex align-items-center"><i className="bi bi-check-circle-fill text-success me-2"></i><span>Instant Download</span></div>
                  </div>

                  {buyButton}

                  {/* Back Button */}
                  <Button variant="outline-secondary" className="w-100 py-2" onClick={() => navigate('/')}><i className="bi bi-arrow-left me-2"></i>Back to Gallery</Button>
                  <hr className="my-4" />
                  {/* Secure Transaction Text */}
                  <div className="text-center"><p className="mb-2"><strong>Secure transaction</strong></p><p className="text-muted small mb-0">Your payment information is processed securely.</p></div>
                </Card.Body>
              </Card>
            </Col>
          </Row>


          <div className="mt-5">
          </div>
        </Container>

        {/* Purchase Confirmation Modal */}
        <Modal show={showPurchaseModal} onHide={handleClosePurchaseModal} centered backdrop="static" keyboard={false}>
          {/* ... Modal content ... */}
          <Modal.Header closeButton={!purchaseLoading} className="border-0">
            <Modal.Title className="fs-4">
              {purchaseSuccess ? "Proceed to Payment" : "Confirm your purchase"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className="px-4">
            {purchaseError && <Alert variant="danger">{purchaseError}</Alert>}
            {purchaseSuccess && !purchaseError && ( /* Step 2: Show PayPal link */
                <div className="text-center py-3">
                  <div className="mb-3"><i className="bi bi-paypal text-primary" style={{ fontSize: '3rem' }}></i></div>
                  <h4 className="mb-3">Ready for PayPal</h4>
                  <p className="mb-4">Click the button below to complete your payment securely via PayPal.</p>
                  <Button variant="info" href={paymentApprovalUrl || '#'} target="_blank" rel="noopener noreferrer" disabled={!paymentApprovalUrl} className="w-100 mb-3"> Go to PayPal </Button>
                  <p className="text-muted small">After completing payment, you'll be redirected back here. If you change your mind, close this window to cancel.</p>
                </div>
            )}
            {!purchaseSuccess && !purchaseError && ( /* Step 1: Show Summary */
                <>
                  <div className="d-flex mb-4">
                    <img src={modalImageUrl} alt={image.imageName || 'Preview'} className="rounded" style={{ width: '80px', height: '80px', objectFit: 'cover' }}/>
                    <div className="ms-3">
                      <h5 className="mb-1">{image.imageName || 'Untitled'}</h5>
                      <p className="text-muted mb-1">By {image.uploader?.username || 'Unknown'}</p>
                      <p className="price mb-0">${image.price?.toFixed(2) || '0.00'}</p>
                    </div>
                  </div>
                  <div className="mb-4">
                    <h5 className="mb-3">Order summary</h5>
                    <div className="d-flex justify-content-between mb-2"><span>Item price</span><span>${image.price?.toFixed(2) || '0.00'}</span></div>
                    <div className="d-flex justify-content-between mb-2"><span>Processing fee</span><span>$0.00</span></div>
                    <hr />
                    <div className="d-flex justify-content-between fw-bold"><span>Total</span><span>${image.price?.toFixed(2) || '0.00'}</span></div>
                  </div>
                  <div className="d-grid">
                    <Button variant="primary" onClick={handlePurchase} disabled={purchaseLoading} size="lg">
                      {purchaseLoading ? <><Spinner as="span" animation="border" size="sm" className="me-2"/>Processing...</> : 'Confirm and Proceed'}
                    </Button>
                  </div>
                </>
            )}
          </Modal.Body>
          {!purchaseLoading && (
              <Modal.Footer className="border-0 justify-content-center">
                <Button variant="outline-secondary" onClick={handleClosePurchaseModal}>
                  {purchaseSuccess ? "Close" : "Cancel"}
                </Button>
              </Modal.Footer>
          )}
        </Modal>
      </>
  );
};

export default ImageDetail;