import axiosInstance from '../utils/axiosInterceptor';


class ImageService {

  getAllImages() {
    return axiosInstance.get(`/images?page=0&size=20&sort=ImageId,desc`); // Use ImageId
  }

  getImageById(id) {
    return axiosInstance.get(`/images/${id}`);
  }

  getImagesByUserId(userId) {
    return axiosInstance.get(`/images/user/${userId}`);
  }



  searchImages(query) {

    const encodedQuery = encodeURIComponent(query);
    console.log(`ImageService: Calling /images/search?q=${encodedQuery}`); // Add log
    return axiosInstance.get(`/images/search?q=${encodedQuery}`);
  }



  createImage(imageData) {
    console.log('ImageService.createImage sending to POST /images:', imageData);
    return axiosInstance.post(`/images`, imageData);
  }

  updateImage(id, imageMetadata) {
    return axiosInstance.put(`/images/${id}`, imageMetadata);
  }

  deleteImage(id) {
    return axiosInstance.delete(`/images/${id}`);
  }
}

export default new ImageService();