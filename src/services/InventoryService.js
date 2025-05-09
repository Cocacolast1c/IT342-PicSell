import axiosInstance from '../utils/axiosInterceptor';

class InventoryService {


    getInventoryByUserId(userId) {
        console.log(`InventoryService: Fetching inventory for userId=${userId}`);
        if (!userId) {
            console.error("InventoryService: User ID is required to fetch inventory.");
            return Promise.reject(new Error("User ID is required to fetch inventory."));
        }
        return axiosInstance.get(`/inventory/${userId}`);
    }

}

export default new InventoryService();