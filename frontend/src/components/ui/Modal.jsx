import { X } from 'lucide-react';
import { useEffect } from 'react';

export const Modal = ({ isOpen, onClose, title, children }) => {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/70 backdrop-blur-sm transition-opacity fade-in"
        onClick={onClose}
      />
      
      {/* Modal panel */}
      <div className="relative bg-dark-bg border border-dark-border rounded-xl shadow-2xl w-full max-w-md flex flex-col max-h-[90vh] slide-up">
        <div className="flex items-center justify-between p-5 border-b border-dark-border">
          <h2 className="text-xl font-heading font-bold text-white">{title}</h2>
          <button 
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors p-1"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
        
        <div className="p-5 overflow-y-auto scrollbar-custom">
          {children}
        </div>
      </div>
    </div>
  );
};
