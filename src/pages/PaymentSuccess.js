import React, { useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {Container, Button, Card, Col, Row} from 'react-bootstrap';

const PaymentSuccess = () => {
    const [searchParams] = useSearchParams();
    const paymentId = searchParams.get('paymentId');

    useEffect(() => {

        console.log("Payment successful for paymentId:", paymentId);
    }, [paymentId]);

    return (
        <Container className="mt-5">
            <Row className="justify-content-center">
                <Col md={8} lg={6}>
                    <Card className="text-center shadow-sm">
                        <Card.Body className="p-4 p-md-5">
                            <div className="mb-4">
                                <i className="bi bi-check-circle-fill text-success" style={{ fontSize: '4rem' }}></i>
                            </div>
                            <h2 className="mb-3">Payment Successful!</h2>
                            <p className="text-muted mb-4">
                                Thank you for your purchase. Your transaction has been completed.
                                {paymentId && ` Your Payment ID is ${paymentId}.`}
                            </p>
                            <p>You can view your purchased images in your account.</p>
                            <div className="d-grid gap-2 d-sm-flex justify-content-sm-center mt-4">
                                <Button as={Link} to="/transactions" variant="primary" className="px-4">
                                    View Purchases
                                </Button>
                                <Button as={Link} to="/" variant="outline-secondary" className="px-4">
                                    Continue Shopping
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default PaymentSuccess;