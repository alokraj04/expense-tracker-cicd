import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Menu } from 'lucide-react';
import { Sidebar } from './Sidebar';

export const Layout = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className="flex h-screen bg-dark-bg text-gray-200 overflow-hidden font-body">
      <Sidebar isOpen={mobileMenuOpen} setMobileMenuOpen={setMobileMenuOpen} />
      
      <div className="flex-1 flex flex-col min-w-0 md:ml-64 relative">
        <header className="bg-dark-card border-b border-dark-border h-16 flex items-center md:hidden px-4 shrink-0">
          <button 
            onClick={() => setMobileMenuOpen(true)}
            className="text-gray-200 hover:text-white p-1"
          >
            <Menu className="w-6 h-6" />
          </button>
          <span className="ml-4 font-heading font-bold text-lg text-primary-light">Expense Tracker</span>
        </header>

        <main className="flex-1 overflow-auto scrollbar-custom p-4 md:p-8">
          <div className="max-w-6xl mx-auto h-full slide-up">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
