import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Nav, Alert, Badge, Button, Tabs, Tab, Spinner } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import TransactionService from '../services/TransactionService';


const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
  if (!base64String) return 'https://via.placeholder.com/300?text=No+Image+Data'; // Placeholder
  const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
  if (typeof base64String === 'string' && base64Regex.test(base64String)) {
    return `data:${mimeType};base64,${base64String}`;
  }
  console.warn("Invalid Base64 string received for transaction image, using placeholder.");
  return 'https://via.placeholder.com/300?text=Invalid+Data';
};

const Transactions = ({ currentUser }) => {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('purchases');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }
    fetchTransactions(activeTab);
  }, [currentUser, activeTab]);

  const fetchTransactions = async (type) => {
    if (!currentUser || !currentUser.userId) return;

    setLoading(true);
    setError(null);
    setTransactions([]);

    try {
      let response;
      if (type === 'purchases') {
        response = await TransactionService.getTransactionsByBuyerId(currentUser.userId);
      } else { // 'sales'
        response = await TransactionService.getTransactionsBySellerId(currentUser.userId);
      }
      console.log(`Workspaceed ${type} raw data:`, response.data);
      setTransactions(response.data || []);
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || `Failed to load your ${type}.`;
      setError(errorMsg);
      console.error(`Error fetching ${type}:`, err.response || err);
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadgeVariant = (status) => {
    switch (status?.toLowerCase()) {
      case 'completed': return 'success';
      case 'pending': return 'warning';
      case 'cancelled': return 'danger';
      default: return 'secondary';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
      <Container className="my-5">
        <div className="mb-4">
          <Link to="/" className="text-decoration-none text-muted">Home</Link>
          {' > '} <span>Your Transactions</span>
        </div>

        <div className="d-flex justify-content-between align-items-center mb-4">
          <h1 className="fs-2 mb-0">Your Transactions</h1>
          <Button as={Link} to="/upload" variant="outline-primary" className="d-none d-md-block"> <i className="bi bi-plus-lg me-2"></i> List a new image </Button>
        </div>

        <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-4 transaction-tabs">
          <Tab eventKey="purchases" title="Purchases"><div className="p-1"></div></Tab>
          <Tab eventKey="sales" title="Sales"><div className="p-1"></div></Tab>
        </Tabs>

        {loading ? (
            <div className="text-center my-5"><Spinner animation="border" style={{ color: 'var(--etsy-orange)' }} role="status"><span className="visually-hidden">Loading...</span></Spinner></div>
        ) : error ? (
            <Alert variant="danger"><i className="bi bi-exclamation-circle me-2"></i>{error}</Alert>
        ) : transactions.length === 0 ? (
            <div className="text-center py-5">
            </div>
        ) : (
            <div className="transaction-list">
              {transactions.map(transaction => {
                const imageUrl = createDataUrl(transaction.image?.imageFile);
                let otherPartyUsername = 'Unknown';
                if (activeTab === 'purchases' && transaction.seller?.username) {
                  otherPartyUsername = transaction.seller.username;
                } else if (activeTab === 'sales' && transaction.buyer?.username) {
                  otherPartyUsername = transaction.buyer.username;
                }
                const uploaderUsername = transaction.image?.uploader?.username || 'Unknown Author';

                return (
                    <Card key={transaction.transactionId} className="transaction-card border-0 shadow-sm mb-4">
                      <Card.Body className="p-0">
                        <Row className="g-0">
                          <Col md={3} lg={2}>
                            <div className="transaction-image-container">
                              <img
                                  src={imageUrl}
                                  alt={transaction.image?.imageName || 'Image'} // Use imageName
                                  className="transaction-image"
                                  onClick={() => navigate(`/images/${transaction.image?.imageId}`)}
                              />
                            </div>
                          </Col>
                          <Col md={9} lg={10}>
                            <div className="p-3 p-md-4">
                              <div className="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                  <h5 className="mb-1">
                                    <Link to={`/images/${transaction.image?.imageId}`} className="text-decoration-none text-dark">
                                      {transaction.image?.imageName || 'Untitled Image'}
                                    </Link>
                                  </h5>
                                  <p className="text-muted small mb-1">
                                    Author: <span className="fw-medium">{uploaderUsername}</span>
                                  </p>
                                  <p className="text-muted small mb-0">
                                    {activeTab === 'purchases' ? 'Seller: ' : 'Buyer: '}
                                    <span className="fw-medium">{otherPartyUsername}</span>
                                  </p>
                                </div>
                                <h5 className="price mb-0">${transaction.image?.price?.toFixed(2) || 'N/A'}</h5>
                              </div>

                              <div className="d-flex flex-wrap justify-content-between align-items-center">
                                <div>
                                  <Badge bg={getStatusBadgeVariant(transaction.saleState)} className="me-2 px-3 py-2"> {transaction.saleState || 'Unknown'} </Badge>
                                  <span className="text-muted small"> <i className="bi bi-calendar3 me-1"></i> {formatDate(transaction.saleDate)} </span> {/* Use saleDate */}
                                </div>

                                <div className="mt-3 mt-md-0">
                                  <Button variant="outline-primary" size="sm" className="me-2" onClick={() => navigate(`/images/${transaction.image?.imageId}`)}> View Details </Button>
                                </div>
                              </div>
                            </div>
                          </Col>
                        </Row>
                      </Card.Body>
                    </Card>
                )
              })}
              <div className="text-center mt-4">
                <Button variant="outline-secondary" onClick={() => navigate('/')} className="px-4">Back to Home</Button>
              </div>
            </div>
        )}
      </Container>
  );
};

export default Transactions;