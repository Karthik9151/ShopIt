import React,{createContext,useContext,useEffect,useMemo,useState} from 'react';
import api,{resetSessionExpiry} from '../services/api';

const AuthContext=createContext(null);
const SESSION_EXPIRED_EVENT='shopit:session-expired';

export function AuthProvider({children}){
 const [user,setUser]=useState(()=>{try{return JSON.parse(sessionStorage.getItem('shopit_user')||'null')}catch{return null}});
 const [token,setToken]=useState(()=>sessionStorage.getItem('shopit_access_token'));
 const [sessionExpired,setSessionExpired]=useState(false);
 const [expiredFrom,setExpiredFrom]=useState('/');

 useEffect(()=>{
  const handleSessionExpired=()=>{
   const currentPath=window.location.pathname;
   sessionStorage.removeItem('shopit_access_token');
   sessionStorage.removeItem('shopit_user');
   setToken(null);
   setUser(null);
   setExpiredFrom(currentPath);
   setSessionExpired(true);
  };
  window.addEventListener(SESSION_EXPIRED_EVENT,handleSessionExpired);
  return ()=>window.removeEventListener(SESSION_EXPIRED_EVENT,handleSessionExpired);
 },[]);

 const save=d=>{
  sessionStorage.setItem('shopit_access_token',d.token);
  sessionStorage.setItem('shopit_user',JSON.stringify(d.user));
  setToken(d.token);
  setUser(d.user);
  setSessionExpired(false);
  setExpiredFrom('/');
  resetSessionExpiry();
 };
 const login=async(email,password)=>{const r=await api.post('/auth/login',{email,password});save(r.data);return r.data.user;};
 const register=async form=>{const r=await api.post('/auth/register',form);save(r.data);return r.data.user;};
 const logout=()=>{
  sessionStorage.removeItem('shopit_access_token');
  sessionStorage.removeItem('shopit_user');
  setToken(null);
  setUser(null);
  setSessionExpired(false);
  setExpiredFrom('/');
  resetSessionExpiry();
 };
 const clearSessionExpired=()=>setSessionExpired(false);
 const updateUser=next=>{sessionStorage.setItem('shopit_user',JSON.stringify(next));setUser(next);};
 const value=useMemo(()=>({
  user,token,login,register,logout,updateUser,isAdmin:user?.role==='ADMIN',
  sessionExpired,expiredFrom,clearSessionExpired
 }),[user,token,sessionExpired,expiredFrom]);
 return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth=()=>useContext(AuthContext);
