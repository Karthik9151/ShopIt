import axios from 'axios';
const api=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL||'http://localhost:8080/api',timeout:12000,headers:{'Content-Type':'application/json'}});
api.interceptors.request.use(config=>{const token=sessionStorage.getItem('shopit_access_token');if(token)config.headers.Authorization='Bearer '+token;return config;});
export default api;