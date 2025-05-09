import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { saveToken } from '../utils/authHeader';
import { Container, Alert, Spinner } from 'react-bootstrap';

const AuthCallback = ({ setCurrentUser }) => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [error, setError] = useState(null);

    useEffect(() => {
        const token = searchParams.get('token');
        const userId = searchParams.get('userId');
        const username = searchParams.get('username');
        const email = searchParams.get('email');
        const roles = searchParams.get('roles');
        const errorParam = searchParams.get('error');

        if (errorParam) {
            console.error('OAuth Error from backend:', errorParam);
            setError(`Authentication failed: ${errorParam}. Please try again.`);
            setTimeout(() => navigate('/login'), 3000);
            return;
        }


        if (token && userId && username && email && roles) {
            try {
                const userData = {
                    token,
                    userId: parseInt(userId, 10),
                    username,
                    email,
                    roles

                };

                if (isNaN(userData.userId)) {
                    throw new Error("Invalid user ID received.");
                }

                saveToken(token, userData);

                setCurrentUser(userData);

                navigate('/', { replace: true });

            } catch (e) {
                console.error('Auth callback processing error:', e);
                setError('Failed to process authentication callback. Please try logging in again.');
                setTimeout(() => navigate('/login'), 3000);
            }
        } else {
            // Handle case where parameters are missing
            console.error('Auth callback error: Missing required parameters in URL.');
            setError('Authentication callback failed: Incomplete information received. Please try logging in again.');
            setTimeout(() => navigate('/login'), 3000);
        }

    }, [searchParams, navigate, setCurrentUser]);

    return (
        <Container className="text-center mt-5">
            {error ? (
                <Alert variant="danger">
                    <h4>Authentication Error</h4>
                    <p>{error}</p>
                    <p>Redirecting to login...</p>
                </Alert>
            ) : (
                <>
                    <h2>Authenticating...</h2>
                    <Spinner animation="border" variant="primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </Spinner>
                </>
            )}
        </Container>
    );
};

export default AuthCallback;