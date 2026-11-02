import axios from 'axios';
import {createSessionExpiryInterceptor} from './sessionExpiry';

const SESSION_EXPIRED_EVENT='shopit:session-expired';
const api=axios.create({
 baseURL:import.meta.env.VITE_API_BASE_URL||'http://localhost:8080/api',
 timeout:12000,
 headers:{'Content-Type':'application/json'}
});

api.interceptors.request.use(config=>{
 const token=sessionStorage.getItem('shopit_access_token');
 if(token)config.headers.Authorization='Bearer '+token;
 return config;
});

const sessionExpiry=createSessionExpiryInterceptor(()=>{
 window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
});

api.interceptors.response.use(
 response=>response,
 error=>sessionExpiry.intercept(error)
);

export function resetSessionExpiry(){
 sessionExpiry.reset();
}

export default api;
