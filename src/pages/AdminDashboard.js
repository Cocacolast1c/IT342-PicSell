import React, { useState, useEffect, useCallback } from 'react';
import { Container, Table, Button, Spinner, Alert, Modal, Form, InputGroup, Badge, Pagination } from 'react-bootstrap';
import ImageService from '../services/ImageService';
import AdminService from '../services/AdminService';
import { useNavigate } from 'react-router-dom';


const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
        return new Date(dateString).toLocaleDateString('en-CA');
    } catch (e) { return 'Invalid Date'; }
};
const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
    if (!base64String) return `https://via.placeholder.com/60x40?text=N/A`;
    const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
    if (typeof base64String === 'string' && base64Regex.test(base64String)) { return `data:${mimeType};base64,${base64String}`; }
    console.warn("Invalid Base64 string received for image preview.");
    return `https://via.placeholder.com/60x40?text=Error`;
};

function AdminDashboard() {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [selectedImage, setSelectedImage] = useState(null);
    const [updateFormData, setUpdateFormData] = useState({ imageName: '', imageDescription: '', price: '', tags: '', licenseType: 'Standard' });
    const [isProcessing, setIsProcessing] = useState(false);
    const [modalError, setModalError] = useState(null);

    const fetchAllImages = useCallback(async (page = currentPage, size = pageSize) => {
        setLoading(true); setError(null);
        try {
            console.log(`Workspaceing page: ${page}, size: ${size}`);
            const response = await AdminService.getAllImages(page, size);
            if (response.data && response.data.content) {
                setImages(response.data.content);
                setTotalPages(response.data.totalPages);
                setCurrentPage(response.data.number);
            } else { setImages([]); setTotalPages(0); }
        } catch (err) { setError(err.response?.data?.message || err.message || "Failed to load images."); setImages([]); setTotalPages(0); }
        finally { setLoading(false); }
    }, [currentPage, pageSize]);

    useEffect(() => { fetchAllImages(currentPage, pageSize); }, [fetchAllImages, currentPage, pageSize]);

    const handleDeleteClick = (image) => { setSelectedImage(image); setModalError(null); setShowDeleteModal(true); };
    const handleConfirmDelete = async () => {
        if (!selectedImage) return;
        setIsProcessing(true); setModalError(null);
        try {
            await ImageService.deleteImage(selectedImage.imageId);
            setShowDeleteModal(false); alert(`Image deleted successfully!`);
            setSelectedImage(null); fetchAllImages(currentPage, pageSize);
        } catch (err) { setModalError(`Delete failed: ${err.response?.data?.message || err.message}`); }
        finally { setIsProcessing(false); }
    };

    const handleUpdateClick = (image) => { setSelectedImage(image); setUpdateFormData({ imageName: image.imageName || '', imageDescription: image.imageDescription || '', price: image.price != null ? image.price.toString() : '0.00', tags: image.tags || '', licenseType: image.licenseType || 'Standard'}); setModalError(null); setShowUpdateModal(true); };
    const handleUpdateFormChange = (e) => { const { name, value } = e.target; setUpdateFormData(prev => ({ ...prev, [name]: value })); };
    const handleConfirmUpdate = async (e) => {
        e.preventDefault(); if (!selectedImage) return;
        const price = parseFloat(updateFormData.price);
        if (isNaN(price) || price < 0) { setModalError("Invalid price."); return; }
        if (!updateFormData.imageName.trim()) { setModalError("Image Name required."); return; }
        setIsProcessing(true); setModalError(null);
        const updatedData = { ...updateFormData, price: price };
        try {
            await ImageService.updateImage(selectedImage.imageId, updatedData);
            setShowUpdateModal(false); alert(`Image updated successfully!`);
            setSelectedImage(null); fetchAllImages(currentPage, pageSize);
        } catch (err) { setModalError(`Update failed: ${err.response?.data?.message || err.message}`); }
        finally { setIsProcessing(false); }
    };

    const handlePageChange = (pageNumber) => { setCurrentPage(pageNumber - 1); };
    let paginationItems = [];
    if (totalPages > 1)

    return (
        <Container fluid className="mt-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h2>Admin Dashboard - Image Management</h2>
            </div>

            {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}

            {loading ? (
                <div className="text-center my-5"><Spinner animation="border" variant="primary" /> Loading Images...</div>
            ) : (
                <>
                    <Table striped bordered hover responsive size="sm" className="mt-3 admin-image-table">
                        <thead>
                        <tr>
                            <th>ID</th><th>Preview</th><th>Name</th><th>Uploader</th><th>Price</th><th>Tags</th><th>License</th><th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {images.length > 0 ? images.map(image => (
                            <tr key={image.imageId}>
                                <td>{image.imageId}</td>
                                <td><img src={createDataUrl(image.imageFile)} alt={`Preview of ${image.imageName || 'image'}`} style={{ width: '60px', height: 'auto', maxHeight: '40px', objectFit: 'cover' }} /></td>
                                <td style={{ minWidth: '150px'}}>{image.imageName}</td>
                                <td>{image.uploader?.username || 'N/A'}{image.uploader?.userId && ` (ID: ${image.uploader.userId})`}</td>
                                <td>${image.price != null ? image.price.toFixed(2) : 'N/A'}</td>
                                <td>{image.tags ? image.tags.split(',').map(tag => <Badge bg="secondary" pill className="me-1 mb-1" key={tag}>{tag.trim()}</Badge>) : 'None'}</td>
                                <td>{image.licenseType || 'N/A'}</td>
                                <td>
                                    <Button variant="outline-primary" size="sm" className="me-1 mb-1" onClick={() => handleUpdateClick(image)} title="Edit Details"><i className="bi bi-pencil-fill"></i></Button>
                                    <Button variant="outline-danger" size="sm" className="me-1 mb-1" onClick={() => handleDeleteClick(image)} title="Delete Image"><i className="bi bi-trash-fill"></i></Button>
                                    <Button variant="outline-info" size="sm" className="mb-1" onClick={() => navigate(`/images/${image.imageId}`)} title="View Details Page"><i className="bi bi-eye-fill"></i></Button>
                                </td>
                            </tr>
                        )) : (<tr><td colSpan="8" className="text-center">No images found.</td></tr>)}
                        </tbody>
                    </Table>
                    {totalPages > 1 && ( <div className="d-flex justify-content-center"><Pagination>{paginationItems}</Pagination></div> )}
                </>
            )}
            {/* Delete Modal */}
            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Confirm Deletion</Modal.Title></Modal.Header>
                <Modal.Body>
                    {modalError && <Alert variant="danger">{modalError}</Alert>}
                    Are you sure you want to delete image "{selectedImage?.imageName}" (ID: {selectedImage?.imageId})?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)} disabled={isProcessing}>Cancel</Button>
                    <Button variant="danger" onClick={handleConfirmDelete} disabled={isProcessing}>{isProcessing ? <Spinner size="sm"/> : 'Delete'}</Button>
                </Modal.Footer>
            </Modal>
            {/* Update Modal */}
            <Modal show={showUpdateModal} onHide={() => setShowUpdateModal(false)} size="lg">
                <Modal.Header closeButton><Modal.Title>Update Image (ID: {selectedImage?.imageId})</Modal.Title></Modal.Header>
                <Form onSubmit={handleConfirmUpdate}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger" onClose={() => setModalError(null)} dismissible>{modalError}</Alert>}
                        <Form.Group className="mb-3"><Form.Label>Name</Form.Label><Form.Control type="text" name="imageName" value={updateFormData.imageName} onChange={handleUpdateFormChange} required /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Description</Form.Label><Form.Control as="textarea" rows={3} name="imageDescription" value={updateFormData.imageDescription} onChange={handleUpdateFormChange} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Price</Form.Label><InputGroup><InputGroup.Text>$</InputGroup.Text><Form.Control type="number" step="0.01" min="0" name="price" value={updateFormData.price} onChange={handleUpdateFormChange} required /></InputGroup></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>Tags</Form.Label><Form.Control type="text" name="tags" value={updateFormData.tags} onChange={handleUpdateFormChange} /></Form.Group>
                        <Form.Group className="mb-3"><Form.Label>License</Form.Label><Form.Select name="licenseType" value={updateFormData.licenseType} onChange={handleUpdateFormChange} required><option value="Standard">Standard</option><option value="Extended">Extended</option><option value="Editorial">Editorial</option></Form.Select></Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowUpdateModal(false)} disabled={isProcessing}>Cancel</Button>
                        <Button variant="primary" type="submit" disabled={isProcessing}>{isProcessing ? <Spinner size="sm"/> : 'Save'}</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

        </Container>
    );
}

export default AdminDashboard;