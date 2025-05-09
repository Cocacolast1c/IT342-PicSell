
export const DEV_MODE = false;

export const DEV_API_URL = 'https://picsell-backend.onrender.com';
export const PROD_API_URL = 'https://picsell-backend.onrender.com';

export const getApiUrl = () => {
  return DEV_MODE ? DEV_API_URL : PROD_API_URL;
};

export const USE_SAMPLE_DATA = DEV_MODE;
