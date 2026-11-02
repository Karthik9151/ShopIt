import axios from 'axios';
import {createSessionExpiryInterceptor} from './sessionExpiry';

const SESSION_EXPIRED_EVENT='shopit:session-expired';
export const API_PENDING_EVENT='shopit:api-pending';
let pendingRequests=0;

function announcePendingRequests(){
 window.dispatchEvent(new CustomEvent(API_PENDING_EVENT,{detail:{pending:pendingRequests}}));
}

function finishPendingRequest(config){
 if(!config?.__shopitPending||config.__shopitPendingFinished)return;
 config.__shopitPendingFinished=true;
 pendingRequests=Math.max(0,pendingRequests-1);
 announcePendingRequests();
}

export function shouldRetryGet(error){
 const config=error?.config;
 if(!config||(config.method||'get').toLowerCase()!=='get'||config.__shopitRetried)return false;
 if(error.response)return [502,503,504].includes(error.response.status);
 return Boolean(error.request)&&error.code!=='ERR_CANCELED'&&error.code!=='ERR_ABORTED';
}

const api=axios.create({
 baseURL:import.meta.env.VITE_API_BASE_URL||'http://localhost:8080/api',
 timeout:12000,
 headers:{'Content-Type':'application/json'}
});

api.interceptors.request.use(config=>{
 const token=sessionStorage.getItem('shopit_access_token');
 if(token)config.headers.Authorization='Bearer '+token;
 if((config.method||'get').toLowerCase()==='get')config.timeout=60000;
 if(!config.__shopitPending){
  config.__shopitPending=true;
  config.__shopitPendingFinished=false;
  pendingRequests++;
  announcePendingRequests();
 }
 return config;
});

const sessionExpiry=createSessionExpiryInterceptor(()=>{
 window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
});

api.interceptors.response.use(
 response=>{
  finishPendingRequest(response.config);
  return response;
 },
 error=>{
  if(shouldRetryGet(error)){
   error.config.__shopitRetried=true;
   return api.request(error.config);
  }
  finishPendingRequest(error.config);
  return sessionExpiry.intercept(error);
 }
);

export function resetSessionExpiry(){
 sessionExpiry.reset();
}

export default api;
