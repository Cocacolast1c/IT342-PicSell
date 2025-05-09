import axiosInstance from '../utils/axiosInterceptor';

class AdminService {


    getAllImages(page = 0, size = 10) {
        console.log(`AdminService: Fetching all images - page: ${page}, size: ${size}`);
        return axiosInstance.get(`/admin/images/all`, {
            params: {
                page: page,
                size: size,

            }
        });
    }

}

export default new AdminService();