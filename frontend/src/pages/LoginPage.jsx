import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Eye, EyeOff, Lock, User } from 'lucide-react';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const success = await login(username, password);
    setLoading(false);
    if (success) {
      navigate(from, { replace: true });
    }
  };

  // ✅ GOOGLE OAUTH HANDLER
  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  return (<div className="min-h-screen flex items-center justify-center bg-dark-bg p-4 slide-up"> <div className="w-full max-w-md bg-dark-card border border-dark-border rounded-2xl shadow-xl overflow-hidden"> <div className="p-8"> <div className="text-center mb-8 justify-center"> <h1 className="text-3xl font-heading font-bold text-white mb-2">Welcome Back</h1> <p className="text-gray-400">Sign in to manage your expenses</p> </div>


    <form onSubmit={handleSubmit} className="space-y-5">
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Username</label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <User className="h-5 w-5 text-gray-500" />
          </div>
          <input
            type="text"
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full pl-10 pr-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition"
            placeholder="Enter your username"
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Password</label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Lock className="h-5 w-5 text-gray-500" />
          </div>
          <input
            type={showPassword ? "text" : "password"}
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full pl-10 pr-10 py-2 bg-dark-surface border border-dark-border rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition"
            placeholder="••••••••"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-white"
          >
            {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
          </button>
        </div>
        <div className="flex justify-end mt-1">
          <Link to="/forgot-password" className="text-xs text-primary-light hover:text-primary transition-colors">
            Forgot password?
          </Link>
        </div>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full py-2.5 px-4 rounded-lg bg-primary hover:bg-primary-hover text-white font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center mt-2"
      >
        {loading ? 'Signing in...' : 'Sign In'}
      </button>
    </form>

    {/* ✅ OR Divider */}
    <div className="flex items-center my-5">
      <div className="flex-1 border-t border-gray-700"></div>
      <span className="px-3 text-sm text-gray-400">OR</span>
      <div className="flex-1 border-t border-gray-700"></div>
    </div>

    {/* ✅ GOOGLE LOGIN BUTTON */}
    <button
      onClick={handleGoogleLogin}
      className="w-full py-2.5 px-4 rounded-lg bg-white text-black font-medium hover:bg-gray-200 transition-colors flex items-center justify-center gap-2"
    >
      Continue with Google
    </button>

    <div className="mt-6 text-center text-sm text-gray-400">
      Don't have an account?{' '}
      <Link to="/signup" className="text-primary-light hover:text-primary font-medium transition-colors">
        Sign up
      </Link>
    </div>
  </div>
  </div>
  </div>


  );
}
