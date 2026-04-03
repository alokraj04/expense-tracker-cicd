import { createContext, useState, useContext, useEffect } from 'react';
import { authService, userService } from '../api';
import toast from 'react-hot-toast';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('accessToken'));
  const [loading, setLoading] = useState(true);

  // Sync token with localStorage (important for OAuth)
  useEffect(() => {
    const storedToken = localStorage.getItem('accessToken');
    if (storedToken && storedToken !== token) {
      setToken(storedToken);
    }
  }, []);

  // Fetch user when token changes
  useEffect(() => {
    const fetchUser = async () => {
      if (token) {
        try {
          const res = await userService.getProfile();
          setUser(res.data);
        } catch (error) {
          console.error('Error fetching user profile', error);
          if (error.response?.status === 401) {
            logout();
          }
        }
      } else {
        setUser(null);
      }
      setLoading(false);
    };

    fetchUser();
  }, [token]);

  const login = async (username, password) => {
    try {
      const res = await authService.login({ username, password });
      const { accessToken, userId } = res.data;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('userId', userId);

      setToken(accessToken); // triggers fetchUser

      toast.success('Logged in successfully!');
      return true;
    } catch (error) {
      toast.error('Login failed. Please check your credentials.');
      return false;
    }
  };

  const signup = async (userData) => {
    try {
      await authService.signup(userData);
      toast.success('Signup successful! Please login.');
      return true;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Signup failed.');
      return false;
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userId');

    // force full reset
    window.location.href = "/login";
  };

  const value = {
    user,
    token,
    loading,
    login,
    signup,
    logout,
    setUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {loading ? (
        <div className="h-screen w-screen flex justify-center items-center bg-dark-bg text-primary-light">
          Loading...
        </div>
      ) : (
        children
      )}
    </AuthContext.Provider>
  );
};