import { useState, useEffect } from 'react';
import { budgetService, categoryService } from '../api';
import { Modal } from '../components/ui/Modal';
import { Plus, Edit2, Trash2, Target, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

const formatCurrency = (amount) => {
  if (amount == null) return "₹0";
  return amount.toLocaleString("en-IN", {
    style: "currency",
    currency: "INR",
  });
};

export default function BudgetsPage() {
  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [formData, setFormData] = useState({ 
    id: null, 
    categoryId: '', 
    budgetLimit: '', 
    period: 'MONTHLY' 
  });

  useEffect(() => {
    fetchCategories();
    fetchBudgets();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await categoryService.getByType('EXPENSE');
      setCategories(res.data);
    } catch (error) {
      console.error("Categories fetch error", error);
    }
  };

  const fetchBudgets = async () => {
    setLoading(true);
    try {
      const res = await budgetService.getAll();
      setBudgets(res.data);
    } catch (error) {
      toast.error('Failed to load budgets');
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({ id: null, categoryId: '', budgetLimit: '', period: 'MONTHLY' });
    setIsModalOpen(true);
  };

  const openEditModal = (budget) => {
    // Requires mapping the categoryName back to ID for edit if backend doesn't send categoryId, 
    // but usually budget object has categoryName. Assuming backend returns categoryId, if not, find it:
    const catId = categories.find(c => c.name === budget.categoryName)?.id || '';
    setFormData({ 
      id: budget.id, 
      categoryId: catId, 
      budgetLimit: budget.budgetLimit, 
      period: budget.budgetPeriod || 'MONTHLY'
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this budget?')) return;
    try {
      await budgetService.delete(id);
      toast.success('Budget deleted');
      fetchBudgets();
    } catch (error) {
      toast.error('Failed to delete budget');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitLoading(true);
    try {
      const payload = {
        categoryId: parseInt(formData.categoryId),
        budgetLimit: parseFloat(formData.budgetLimit),
        period: formData.period
      };

      if (formData.id) {
        await budgetService.update(formData.id, payload);
        toast.success('Budget updated');
      } else {
        await budgetService.create(payload);
        toast.success('Budget recorded');
      }
      setIsModalOpen(false);
      fetchBudgets();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save budget');
    } finally {
      setSubmitLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'OK': return 'bg-success text-success';
      case 'WARNING': return 'bg-warning text-warning';
      case 'EXCEEDED': return 'bg-danger text-danger';
      default: return 'bg-primary-light text-primary-light';
    }
  };

  return (
    <div className="space-y-6 slide-up">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold font-heading">Budgets</h1>
        <button 
          onClick={openAddModal}
          className="bg-primary hover:bg-primary-hover text-white py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium w-fit"
        >
          <Plus className="w-4 h-4 mr-2" /> Add Budget
        </button>
      </div>

      {loading ? (
        <div className="flex h-32 items-center justify-center text-gray-500">Loading budgets...</div>
      ) : budgets.length === 0 ? (
        <div className="bg-dark-card border border-dark-border rounded-xl p-8 text-center text-gray-400">
          No budgets configured yet. Establish limits by adding a new budget.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {budgets.map((b) => {
            const colors = getStatusColor(b.budgetStatus).split(' ');
            const barColor = colors[0];
            const textColor = colors[1];
            
            return (
              <div key={b.id} className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm relative group hover:border-primary transition-colors">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="font-heading font-bold text-lg text-white flex items-center">
                      <Target className="w-5 h-5 mr-2 text-primary-light" />
                      {b.categoryName}
                    </h3>
                    <span className="inline-block mt-1 px-2 py-0.5 rounded text-xs font-medium bg-dark-surface border border-dark-border text-gray-400">
                      {b.budgetPeriod}
                    </span>
                  </div>
                  <div className={`px-2.5 py-1 rounded-full text-xs font-bold uppercase tracking-wide border bg-opacity-10 border-opacity-20 ${textColor} border-current bg-current`}>
                    {b.budgetStatus}
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Spent: <span className="text-white font-medium">{formatCurrency(b.spent)}</span></span>
                    <span className="text-gray-400">Limit: <span className="text-white font-medium">{formatCurrency(b.budgetLimit)}</span></span>
                  </div>
                  <div className="w-full bg-dark-surface rounded-full h-2.5 outline outline-1 outline-dark-border">
                    <div 
                      className={`h-2.5 rounded-full ${barColor} transition-all duration-500`} 
                      style={{ width: `${Math.min(b.percentageUsed, 100)}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between text-xs text-gray-500 font-medium">
                    <span>{b.percentageUsed.toFixed(1)}% used</span>
                    <span>Remaining: {formatCurrency(b.remaining)}</span>
                  </div>
                </div>

                <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute right-4 bottom-4 flex gap-2">
                  <button onClick={() => openEditModal(b)} className="p-1.5 bg-dark-surface rounded-md text-gray-400 hover:text-primary transition-colors hover:bg-dark-border">
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button onClick={() => handleDelete(b.id)} className="p-1.5 bg-dark-surface rounded-md text-gray-400 hover:text-danger transition-colors hover:bg-dark-border">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={formData.id ? 'Edit Budget' : 'Add Budget'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Category</label>
            <select required value={formData.categoryId} onChange={(e) => setFormData({...formData, categoryId: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary flex-1">
              <option value="" disabled>Select Expense Category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Budget Limit</label>
            <input type="number" step="0.01" required value={formData.budgetLimit} onChange={(e) => setFormData({...formData, budgetLimit: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" placeholder="0.00" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Period</label>
            <select required value={formData.period} onChange={(e) => setFormData({...formData, period: e.target.value})} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary">
              <option value="MONTHLY">Monthly</option>
              <option value="WEEKLY">Weekly</option>
            </select>
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
             <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white">Cancel</button>
             <button type="submit" disabled={submitLoading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium">Save</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
