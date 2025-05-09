
export function authHeader() {

  const user = JSON.parse(localStorage.getItem('user'));

  if (user && user.token) {
    return { Authorization: `Bearer ${user.token}` };
  } else {
    return {};
  }
}

export function saveToken(token, userData) {
  const user = { ...userData, token };
  localStorage.setItem('user', JSON.stringify(user));
}

export function getToken() {
  const user = JSON.parse(localStorage.getItem('user'));
  return user?.token || null;
}

export function removeToken() {
  localStorage.removeItem('user');
}

export function isTokenExpired() {
  const token = getToken();
  if (!token) return true;
  
  try {
    const payload = token.split('.')[1];
    const decodedPayload = JSON.parse(atob(payload));
    return decodedPayload.exp * 1000 < Date.now();
  } catch (error) {
    return true;
  }
}
