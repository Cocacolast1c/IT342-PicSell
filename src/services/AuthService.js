import axiosInstance from '../utils/axiosInterceptor';
import { saveToken, removeToken } from '../utils/authHeader';
import { getApiUrl } from '../config';

class AuthService {
  login(username, password) {
    return axiosInstance
        .post(`/users/login`, { username, password })
        .then(response => {
          if (response.data && response.data.token) {
            saveToken(response.data.token, response.data);
          }
          return response.data;
        });
  }

  logout() {
    removeToken();
  }

  register(username, email, password) {
    return axiosInstance.post(`/users/register`, {
      username,
      email,
      password
    });
  }

  getCurrentUser() {
    const user = localStorage.getItem('user');
    try {
      return user ? JSON.parse(user) : null;
    } catch (e) {
      console.error("Failed to parse user from localStorage", e);
      removeToken();
      return null;
    }
  }


  updateUser(id, userDetails) {
    return axiosInstance.put(`/users/${id}`, userDetails)
        .then(response => {
          const currentUser = this.getCurrentUser();
          if (currentUser && response.data) {
            const updatedUserData = { ...currentUser, ...response.data, token: currentUser.token };
            delete updatedUserData.password;
            saveToken(updatedUserData.token, updatedUserData);
          }
          return response;
        });
  }

  redirectToGoogleLogin() {
    window.location.href = `${getApiUrl()}/oauth2/authorization/google`;
  }
}

export default new AuthService();