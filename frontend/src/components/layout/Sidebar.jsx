import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  WalletCards, 
  ArrowDownToLine, 
  ArrowUpFromLine, 
  PieChart, 
  Tags, 
  UserCircle 
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const NAV_ITEMS = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/expenses', label: 'Expenses', icon: ArrowDownToLine },
  { path: '/incomes', label: 'Incomes', icon: ArrowUpFromLine },
  { path: '/budgets', label: 'Budgets', icon: WalletCards },
  { path: '/categories', label: 'Categories', icon: Tags },
  { path: '/analytics', label: 'Analytics', icon: PieChart },
];

export const Sidebar = ({ isOpen, setMobileMenuOpen }) => {
  const { user, logout } = useAuth();

  return (
    <>
      <div 
        className={`fixed inset-y-0 left-0 z-20 w-64 bg-dark-card border-r border-dark-border transform transition-transform duration-300 ease-in-out md:translate-x-0 ${isOpen ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <div className="flex flex-col h-full">
          <div className="flex items-center justify-center h-16 border-b border-dark-border px-4">
            <h1 className="text-xl font-heading font-bold text-primary-light">Expense Tracker</h1>
          </div>
          
          <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto scrollbar-custom">
            {NAV_ITEMS.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center px-4 py-3 rounded-lg transition-colors ${
                      isActive 
                        ? 'bg-primary text-white' 
                        : 'text-gray-400 hover:bg-dark-surface hover:text-white'
                    }`
                  }
                >
                  <Icon className="w-5 h-5 mr-3" />
                  <span className="font-medium">{item.label}</span>
                </NavLink>
              );
            })}
            
            <div className="pt-4 mt-4 border-t border-dark-border">
              <NavLink
                to="/profile"
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center px-4 py-3 rounded-lg transition-colors ${
                    isActive 
                      ? 'bg-primary text-white' 
                      : 'text-gray-400 hover:bg-dark-surface hover:text-white'
                  }`
                }
              >
                <UserCircle className="w-5 h-5 mr-3" />
                <span className="font-medium">Profile</span>
              </NavLink>
            </div>
          </nav>

          <div className="p-4 border-t border-dark-border">
            <div className="flex items-center mb-4 px-2">
              <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold uppercase shrink-0">
                {user?.name?.charAt(0) || user?.username?.charAt(0) || '?'}
              </div>
              <div className="ml-3 truncate">
                <p className="text-sm font-medium text-white truncate">{user?.name}</p>
                <p className="text-xs text-gray-400 truncate">@{user?.username}</p>
              </div>
            </div>
            <button 
              onClick={logout}
              className="w-full py-2 px-4 bg-dark-surface hover:bg-danger hover:text-white text-gray-300 rounded-lg transition-colors text-sm font-medium"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
      
      {/* Mobile overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-10 bg-black/50 md:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}
    </>
  );
};
