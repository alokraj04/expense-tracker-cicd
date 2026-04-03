import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../api';
import { Modal } from '../components/ui/Modal';
import { User, Mail, Shield, Key, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { user, setUser, logout } = useAuth();
  
  // Update Profile State
  const [isEditProfileModalOpen, setIsEditProfileModalOpen] = useState(false);
  const [profileData, setProfileData] = useState({ name: user?.name || '' });
  
  // Change Password State
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [passwordData, setPasswordData] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });

  // Email Change State
  const [newEmail, setNewEmail] = useState('');

  // Forgot Password State
  const [isForgotPasswordModalOpen, setIsForgotPasswordModalOpen] = useState(false);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');

  // Delete Account State
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');

  const [loading, setLoading] = useState(false);

  const getRoleColor = (role) => {
    switch(role) {
      case 'ADMIN': return 'bg-amber-500/20 text-amber-500 border-amber-500/30';
      case 'AUDITOR': return 'bg-blue-500/20 text-blue-500 border-blue-500/30';
      case 'USER': return 'bg-success/20 text-success border-success/30';
      default: return 'bg-gray-500/20 text-gray-400 border-gray-500/30';
    }
  };

  const getProviderColor = (provider) => {
    switch(provider) {
      case 'LOCAL': return 'bg-gray-700 text-gray-300';
      case 'GOOGLE': return 'bg-blue-600 text-white';
      case 'GITHUB': return 'bg-purple-600 text-white';
      default: return 'bg-gray-700 text-gray-300';
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userService.updateProfile(profileData);
      setUser({ ...user, name: profileData.name });
      toast.success('Profile updated successfully');
      setIsEditProfileModalOpen(false);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handleRequestEmailChange = async (e) => {
    e.preventDefault();
    if (!newEmail) return toast.error('Please enter a new email');
    setLoading(true);
    try {
      await userService.requestEmailChange(newEmail);
      toast.success('Verification link sent to your new email');
      setNewEmail(''); // Reset input
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to request email change');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword.length < 6) {
      return toast.error('New password must be at least 6 characters');
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      return toast.error('Passwords do not match');
    }
    setLoading(true);
    try {
      await userService.changePassword(passwordData);
      toast.success('Password changed successfully');
      setIsPasswordModalOpen(false);
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to change password');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if (!forgotPasswordEmail) return toast.error('Please enter your email');
    setLoading(true);
    try {
      await userService.forgotPassword(forgotPasswordEmail);
      toast.success('Password reset link sent to your email');
      setIsForgotPasswordModalOpen(false);
      setForgotPasswordEmail('');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to send reset link');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await userService.deleteAccount(deletePassword || undefined);
      toast.success('Account deleted successfully');
      logout();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete account');
    } finally {
      setLoading(false);
      setIsDeleteModalOpen(false);
    }
  };

  if (!user) return null;

  return (
    <div className="space-y-6 max-w-4xl slide-up">
      <h1 className="text-2xl font-bold font-heading">Account Profile</h1>

      <div className="bg-dark-card border border-dark-border rounded-xl p-6 sm:p-8 shadow-sm">
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
          <div className="w-24 h-24 rounded-full bg-primary flex items-center justify-center text-4xl text-white font-bold uppercase shrink-0 border-4 border-dark-bg">
            {user.name?.charAt(0) || user.username?.charAt(0) || '?'}
          </div>
          
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-white mb-1">{user.name}</h2>
            <p className="text-primary-light mb-4 text-sm font-medium">@{user.username}</p>
            
            <div className="flex flex-wrap gap-2 text-sm text-gray-300">
              <span className="flex items-center bg-dark-surface px-3 py-1.5 rounded-lg border border-dark-border">
                <Mail className="w-4 h-4 mr-2 text-gray-400" /> {user.email || 'No email provided'}
              </span>
              <span className={`px-3 py-1.5 rounded-lg border text-xs font-bold uppercase tracking-wide flex items-center ${getRoleColor(user.roles?.[0] || 'USER')}`}>
                <Shield className="w-3.5 h-3.5 mr-1.5" /> {user.roles?.[0] || 'USER'}
              </span>
              <span className={`px-3 py-1.5 rounded-lg text-xs font-bold uppercase tracking-wide flex items-center ${getProviderColor(user.providerType || 'LOCAL')}`}>
                {user.providerType || 'LOCAL'}
              </span>
            </div>
          </div>
          
          <div className="flex flex-col items-stretch gap-3 w-full sm:w-auto mt-4 sm:mt-0">
            <button 
              onClick={() => {
                setProfileData({ name: user.name || '' });
                setIsEditProfileModalOpen(true);
              }}
              className="bg-dark-surface hover:bg-dark-border text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors border border-dark-border flex justify-center items-center"
            >
              <User className="w-4 h-4 mr-2" /> Edit Profile
            </button>
            <button 
              onClick={() => setIsPasswordModalOpen(true)}
              className="bg-dark-surface hover:bg-dark-border text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors border border-dark-border flex justify-center items-center"
            >
              <Key className="w-4 h-4 mr-2" /> Change Password
            </button>
          </div>
        </div>
      </div>

      {/* Change Email Section */}
      <div className="bg-dark-card border border-dark-border rounded-xl p-6 sm:p-8 shadow-sm">
        <h3 className="text-lg font-heading font-bold text-white mb-2">Change Email</h3>
        <p className="text-gray-400 text-sm mb-4">
          You must verify the new email via a link sent to your inbox before it takes effect.
        </p>
        <form onSubmit={handleRequestEmailChange} className="flex flex-col sm:flex-row gap-3">
          <input 
            type="email" 
            placeholder="Enter new email address" 
            value={newEmail} 
            onChange={(e) => setNewEmail(e.target.value)} 
            className="flex-1 px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary"
            required 
          />
          <button 
            type="submit" 
            disabled={loading}
            className="bg-primary hover:bg-primary-hover text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors whitespace-nowrap disabled:opacity-50"
          >
            Request Email Change
          </button>
        </form>
      </div>

      {/* Danger Zone */}
      <div className="bg-dark-card border border-danger/30 rounded-xl p-6 sm:p-8 shadow-sm">
        <h3 className="text-lg font-heading font-bold text-danger mb-2">Danger Zone</h3>
        <p className="text-gray-400 text-sm mb-4">
          Once you delete your account, there is no going back. All your expenses, incomes, and budgets will be permanently destroyed.
        </p>
        <button 
          onClick={() => setIsDeleteModalOpen(true)}
          className="bg-danger hover:bg-red-600 text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors flex items-center"
        >
          <Trash2 className="w-4 h-4 mr-2" /> Delete Account
        </button>
      </div>

      {/* Edit Profile Modal */}
      <Modal isOpen={isEditProfileModalOpen} onClose={() => setIsEditProfileModalOpen(false)} title="Edit Profile">
        <form onSubmit={handleUpdateProfile} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Full Name</label>
            <input type="text" required value={profileData.name} onChange={(e) => setProfileData({...profileData, name: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" />
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
             <button type="button" onClick={() => setIsEditProfileModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white">Cancel</button>
             <button type="submit" disabled={loading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium">Save Changes</button>
          </div>
        </form>
      </Modal>

      {/* Change Password Modal */}
      <Modal isOpen={isPasswordModalOpen} onClose={() => setIsPasswordModalOpen(false)} title="Change Password">
        <form onSubmit={handleChangePassword} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Current Password</label>
            <input type="password" required value={passwordData.currentPassword} onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">New Password</label>
            <input type="password" required value={passwordData.newPassword} onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" placeholder="Min 6 characters" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Confirm New Password</label>
            <input type="password" required value={passwordData.confirmPassword} onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" />
          </div>
          <div className="flex items-center justify-between pt-4 border-t border-dark-border mt-4">
             <button 
                type="button" 
                onClick={() => {
                  setIsPasswordModalOpen(false);
                  setIsForgotPasswordModalOpen(true);
                }} 
                className="text-sm text-primary-light hover:text-primary hover:underline transition-colors focus:outline-none"
             >
               Forgot password?
             </button>
             <div className="flex gap-2">
               <button type="button" onClick={() => setIsPasswordModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white focus:outline-none">Cancel</button>
               <button type="submit" disabled={loading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium focus:outline-none">Update</button>
             </div>
          </div>
        </form>
      </Modal>

      {/* Forgot Password Modal */}
      <Modal isOpen={isForgotPasswordModalOpen} onClose={() => setIsForgotPasswordModalOpen(false)} title="Reset Password">
        <form onSubmit={handleForgotPassword} className="space-y-4">
          <p className="text-sm text-gray-300">Enter your email address and we'll send you a link to reset your password.</p>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Email Address</label>
            <input type="email" required value={forgotPasswordEmail} onChange={(e) => setForgotPasswordEmail(e.target.value)} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" placeholder="your@email.com" />
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
             <button type="button" onClick={() => setIsForgotPasswordModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white focus:outline-none">Cancel</button>
             <button type="submit" disabled={loading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium focus:outline-none">Send Link</button>
          </div>
        </form>
      </Modal>

      {/* Delete Account Modal */}
      <Modal isOpen={isDeleteModalOpen} onClose={() => setIsDeleteModalOpen(false)} title="Confirm Account Deletion">
        <form onSubmit={handleDeleteAccount} className="space-y-4">
          <p className="text-sm text-gray-300">
            Please type your password to confirm account deletion. If you registered using Google or GitHub, you can leave this blank.
          </p>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Verify Password (Optional for OAuth)</label>
            <input type="password" value={deletePassword} onChange={(e) => setDeletePassword(e.target.value)} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-danger" placeholder="Your password" />
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
             <button type="button" onClick={() => setIsDeleteModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white focus:outline-none">Cancel</button>
             <button type="submit" disabled={loading} className="px-6 py-2 bg-danger hover:bg-red-600 text-white rounded-lg disabled:opacity-50 font-medium focus:outline-none">Delete</button>
          </div>
        </form>
      </Modal>

    </div>
  );
}
