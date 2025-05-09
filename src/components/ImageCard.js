import React from 'react';
import { Card, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const createDataUrl = (base64String, mimeType = 'image/jpeg') => {
  if (!base64String) return `https://via.placeholder.com/300x200?text=No+Image+Data`;
  return `data:${mimeType};base64,${base64String}`;
};

const ImageCard = ({ image }) => {
  const navigate = useNavigate();

  if (!image) return null;

  const handleClick = () => {
    if (image.imageId) {
      navigate(`/images/${image.imageId}`);
    }
  };

  const getRandomBadge = () => {
    // Randomly assign a badge based on imageId
    const badges = [
      { text: 'Bestseller', variant: 'warning' },
      { text: 'New', variant: 'info' },
      { text: 'Sale', variant: 'danger' }
    ];
    if (image.imageId && image.imageId % 3 === 0) {
      const randomIndex = Math.floor(Math.random() * badges.length);
      return badges[randomIndex];
    }
    return null;
  };

  const badge = getRandomBadge();
  const imageUrl = createDataUrl(image.imageFile);

  return (
      <Card className="h-100 image-card cursor-pointer border-0" onClick={handleClick}>
        <div className="position-relative">
          <Card.Img
              variant="top"
              src={imageUrl} // Use Data URL
              alt={image.imageName || 'Image'} // Use imageName
              className="rounded"
          />
          {badge && (
              <Badge
                  bg={badge.variant}
                  className="position-absolute top-0 start-0 m-2 px-2 py-1"
              >
                {badge.text}
              </Badge>
          )}
        </div>
        <Card.Body className="px-0 pt-3 pb-0">
          <Card.Title className="fs-6 text-truncate mb-1">{image.imageName || 'Untitled'}</Card.Title>
          <Card.Text className="price mb-1">
            ${image.price?.toFixed(2) || '0.00'}
          </Card.Text>
          <Card.Text className="text-muted small mb-0">
            By {image.uploader?.username || 'Unknown'}
          </Card.Text>
        </Card.Body>
      </Card>
  );
};

export default ImageCard;