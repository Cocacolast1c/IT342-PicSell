import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useSearchParams } from 'react-router-dom';
import ImageCard from '../components/ImageCard';
import ImageService from '../services/ImageService';


const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
  if (!base64String) return null;
  return `data:${mimeType};base64,${base64String}`;
};

const Search = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [searchTerm, setSearchTerm] = useState(initialQuery);
  const [submittedSearchTerm, setSubmittedSearchTerm] = useState(initialQuery);
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState(!!initialQuery);

  useEffect(() => {
    if (initialQuery) {
      performSearch(initialQuery);
    }
  }, []);


  const performSearch = async (query) => {
    if (!query || !query.trim()) {
      setError('Please enter a search term');
      setSearchResults([]);
      setSearched(true);
      setSubmittedSearchTerm(query);
      return;
    }

    setLoading(true);
    setError(null);
    setSubmittedSearchTerm(query);
    setSearchParams({ q: query });

    try {
      // Call backend search endpoint via service
      const response = await ImageService.searchImages(query);
      setSearchResults(response.data || []);
    } catch (err) {
      setError('Failed to perform search. Please try again later.');
      console.error('Search error:', err);
      setSearchResults([]);
    } finally {
      setLoading(false);
      setSearched(true);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    performSearch(searchTerm);
  };

  return (
      <Container className="my-5">
        <h2 className="mb-4">Search Images</h2>

        <Form onSubmit={handleSearchSubmit} className="mb-4">
          <Row>
            <Col md={9}>
              <Form.Control
                  type="text"
                  placeholder="Search for images..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
              />
            </Col>
            <Col md={3}>
              <Button
                  variant="primary"
                  type="submit"
                  className="w-100"
                  disabled={loading}
              >
                {loading ? <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true"/> Searching...</> : 'Search'}
              </Button>
            </Col>
          </Row>
        </Form>


        {error && <Alert variant="danger">{error}</Alert>}

        {searched && !loading && (
            <>
              <h3 className="mb-3">
                {searchResults.length > 0 ? `Results for "${submittedSearchTerm}"` : `No results found for "${submittedSearchTerm}"`}
              </h3>

              {searchResults.length === 0 && submittedSearchTerm && (
                  <Alert variant="info">
                    Try refining your search term.
                  </Alert>
              )}

              {searchResults.length > 0 && (
                  <>
                    <p className="text-muted mb-4">Found {searchResults.length} image(s).</p>
                    <Row xs={1} md={2} lg={3} className="g-4">
                      {searchResults.map(image => (
                          <Col key={image.imageId}>
                            <ImageCard image={image} />
                          </Col>
                      ))}
                    </Row>
                  </>
              )}
            </>
        )}
      </Container>
  );
};

export default Search;