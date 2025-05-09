import axiosInstance from '../utils/axiosInterceptor';

class TransactionService {
  getAllTransactions() {
    return axiosInstance.get(`/transactions`);
  }

  getTransactionById(id) {
    return axiosInstance.get(`/transactions/${id}`);
  }

  getTransactionsByBuyerId(buyerId) {
    return axiosInstance.get(`/transactions/buyer/${buyerId}`);
  }

  getTransactionsBySellerId(sellerId) {
    return axiosInstance.get(`/transactions/seller/${sellerId}`);
  }

  createTransaction(buyerId, sellerId, imageId) {
    const minimalTransactionData = {
      buyer: { userId: buyerId },
      seller: { userId: sellerId },
      image: { imageId: imageId }
    };
    return axiosInstance.post(`/transactions`, minimalTransactionData);
  }

  updateSaleState(transactionId, saleState) {
    console.log(`TransactionService: Calling PUT /transactions/${transactionId}/sale-state?saleState=${saleState}`);
    return axiosInstance.put(`/transactions/${transactionId}/sale-state?saleState=${saleState}`);
  }

  makePayment(amount, transactionId) {
    console.log(`TransactionService: Calling POST /transactions/pay with amount=${amount}, transactionId=${transactionId}`);
    return axiosInstance.post(`/transactions/pay?amount=${amount}&transactionId=${transactionId}`);
  }
}

export default new TransactionService();