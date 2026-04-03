import { useState } from 'react';
import { Link } from 'react-router-dom';
import { userService } from '../api';
import { Mail, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userService.forgotPassword(email);
      setSubmitted(true);
      toast.success('Password reset link sent to your email.');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to send reset email.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-dark-bg p-4 slide-up">
      <div className="w-full max-w-md bg-dark-card border border-dark-border rounded-2xl shadow-xl overflow-hidden">
        <div className="p-8">
          <div className="mb-6">
            <Link to="/login" className="inline-flex items-center text-sm text-gray-400 hover:text-white transition-colors">
              <ArrowLeft className="w-4 h-4 mr-1" /> Back to Login
            </Link>
          </div>
          
          <div className="text-center mb-8">
            <h1 className="text-3xl font-heading font-bold text-white mb-2">Forgot Password</h1>
            <p className="text-gray-400">
              {submitted ? "Check your email for reset instructions." : "Enter your email to receive a password reset link."}
            </p>
          </div>
          
          {!submitted ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Email Address</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Mail className="h-5 w-5 text-gray-500" />
                  </div>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full pl-10 pr-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition"
                    placeholder="name@example.com"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 px-4 rounded-lg bg-primary hover:bg-primary-hover text-white font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed mt-2"
              >
                {loading ? 'Sending...' : 'Send Reset Link'}
              </button>
            </form>
          ) : (
            <div className="bg-dark-surface p-4 rounded-lg border border-dark-border text-center">
              <p className="text-primary-light font-medium mb-4">Reset Email Sent Successfully</p>
              <button
                onClick={() => setSubmitted(false)}
                className="text-sm text-gray-400 hover:text-white transition-colors underline"
              >
                Send again
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
