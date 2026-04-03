import { useState, useEffect } from 'react';
import { expenseService, categoryService } from '../api';
import { Modal } from '../components/ui/Modal';
import { Plus, Edit2, Trash2, Search, Filter, Upload, FileText, Calendar as CalendarIcon, RefreshCw } from 'lucide-react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import toast from 'react-hot-toast';

const formatDate = (date) => {
  if (!date) return "—";
  const d = new Date(date);
  if (isNaN(d.getTime())) return "—";
  return d.toLocaleDateString("en-IN");
};

const formatCurrency = (amount) => {
  if (amount == null) return "—";
  return amount.toLocaleString("en-IN", {
    style: "currency",
    currency: "INR",
  });
};

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [filterCategoryId, setFilterCategoryId] = useState('');
  const [dateRange, setDateRange] = useState([null, null]);
  const [startDate, endDate] = dateRange;

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [formData, setFormData] = useState({
    id: null,
    amount: '',
    categoryId: '',
    date: new Date().toISOString().split('T')[0],
    note: ''
  });

  // Recurring Modal
  const [isRecurringModalOpen, setIsRecurringModalOpen] = useState(false);
  const [recurringData, setRecurringData] = useState({
    amount: '', categoryId: '', frequency: 'MONTHLY', startDate: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    fetchCategories();
    fetchExpenses();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await categoryService.getByType('EXPENSE');
      setCategories(res.data);
    } catch (error) {
      console.error("Categories fetch error", error);
    }
  };

  const fetchExpenses = async () => {
    setLoading(true);
    try {
      const res = await expenseService.getAll();
      console.log("EXPENSE DATA:", res.data);
      setExpenses(res.data);
    } catch (error) {
      toast.error('Failed to load expenses');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterSearch = async () => {
    setLoading(true);
    try {
      if (startDate && endDate) {
        const res = await expenseService.getDateRange(startDate, endDate);
        let fetchedExpenses = res.data;
        if (filterCategoryId) {
          fetchedExpenses = fetchedExpenses.filter(e => e.categoryId.toString() === filterCategoryId.toString());
        }
        setExpenses(fetchedExpenses);
      } else if (filterCategoryId) {
        const res = await expenseService.getByCategory(filterCategoryId);
        setExpenses(res.data);
      } else {
        await fetchExpenses();
      }
    } catch (error) {
      toast.error('Failed to filter expenses');
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setFilterCategoryId('');
    setDateRange([null, null]);
    fetchExpenses();
  };

  const openAddModal = () => {
    setFormData({ id: null, amount: '', categoryId: '', date: new Date().toISOString().split('T')[0], note: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (expense) => {
    const targetDate = expense.expenseDate || expense.date || expense.createdAt;
    setFormData({
      id: expense.id,
      amount: expense.amount,
      categoryId: expense.categoryId || expense.category?.id || '',
      date: targetDate ? targetDate.split('T')[0] : new Date().toISOString().split('T')[0],
      note: expense.note || ''
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expense?')) return;
    try {
      await expenseService.delete(id);
      toast.success('Expense deleted');
      fetchExpenses();
    } catch (error) {
      toast.error('Failed to delete expense');
    }
  };

  const handleReceiptUpload = async (id, file) => {
    if (!file) return;
    try {
      await expenseService.uploadReceipt(id, file);
      toast.success('Receipt uploaded successfully');
      fetchExpenses();
    } catch (error) {
      toast.error('Failed to upload receipt');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Form validation
    if (!formData.categoryId) {
      toast.error('Please select a category');
      return;
    }
    if (!formData.amount || formData.amount <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }
    if (!formData.date) {
      toast.error('Please select a date');
      return;
    }

    setSubmitLoading(true);
    try {
      let payload;
      if (formData.id) {
        payload = {
          ...formData, // retain full update object context
          amount: Number(formData.amount),
          categoryId: Number(formData.categoryId),
          expenseDate: formData.date ? new Date(formData.date).toISOString() : null
        };
        delete payload.date; // Remove "date" field
      } else {
        payload = {
          amount: Number(formData.amount),
          categoryId: Number(formData.categoryId),
          note: formData.note,
          expenseDate: formData.date ? new Date(formData.date).toISOString() : null
        };
      }

      console.log("FINAL PAYLOAD:", payload);

      if (formData.id) {
        const response = await expenseService.update(formData.id, payload);
        console.log("Updated Expense:", response.data);
        const updatedExpense = response.data;
        if (updatedExpense) {
          setExpenses((prev) =>
            prev.map((item) => (item.id === updatedExpense.id ? updatedExpense : item))
          );
        }
        toast.success('Expense updated');
      } else {
        await expenseService.create(payload);
        toast.success('Expense created');
      }
      setIsModalOpen(false);
      await fetchExpenses();
      console.log("EXPENSE LIST:", expenses);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save expense');
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleRecurringSubmit = async (e) => {
    e.preventDefault();

    // Form validation
    if (!recurringData.categoryId) {
      toast.error('Please select a category');
      return;
    }
    if (!recurringData.amount || recurringData.amount <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }
    if (!recurringData.startDate) {
      toast.error('Please select a start date');
      return;
    }

    setSubmitLoading(true);
    try {
      const payload = {
        amount: Number(recurringData.amount),
        categoryId: Number(recurringData.categoryId),
        frequency: recurringData.frequency,
        startDate: new Date(recurringData.startDate).toISOString()
      };

      console.log("FINAL PAYLOAD:", payload);

      await expenseService.createRecurring(payload);
      toast.success('Recurring expense set up');
      setIsRecurringModalOpen(false);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save recurring expense');
    } finally {
      setSubmitLoading(false);
    }
  };


  return (
    <div className="space-y-6 slide-up">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold font-heading">Expenses</h1>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsRecurringModalOpen(true)}
            className="bg-dark-surface hover:bg-dark-border text-gray-200 py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium border border-dark-border"
          >
            <RefreshCw className="w-4 h-4 mr-2" /> Recurring
          </button>
          <button
            onClick={openAddModal}
            className="bg-primary hover:bg-primary-hover text-white py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium"
          >
            <Plus className="w-4 h-4 mr-2" /> Add Expense
          </button>
        </div>
      </div>

      <div className="bg-dark-card border border-dark-border rounded-xl p-4 flex flex-col md:flex-row items-end gap-4 shadow-sm">
        <div className="w-full md:w-auto">
          <label className="block text-xs font-medium text-gray-400 mb-1">Category</label>
          <select
            value={filterCategoryId}
            onChange={(e) => setFilterCategoryId(e.target.value)}
            className="w-full md:w-48 px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-sm text-white focus:outline-none focus:border-primary"
          >
            <option value="">All Categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>

        <div className="w-full md:w-auto">
          <label className="block text-xs font-medium text-gray-400 mb-1">Date Range</label>
          <div className="flex items-center">
            <DatePicker
              selectsRange={true}
              startDate={startDate}
              endDate={endDate}
              onChange={(update) => setDateRange(update)}
              isClearable={true}
              placeholderText="Select date range"
              className="w-full md:w-56 px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-sm text-white focus:outline-none focus:border-primary"
            />
          </div>
        </div>

        <div className="flex gap-2 w-full md:w-auto">
          <button onClick={handleFilterSearch} className="px-4 py-2 bg-dark-surface border border-dark-border text-white rounded-lg hover:bg-dark-border transition-colors text-sm flex items-center">
            <Search className="w-4 h-4 mr-1" /> Filter
          </button>
          <button onClick={clearFilters} className="px-4 py-2 text-gray-400 hover:text-white transition-colors text-sm">
            Clear
          </button>
        </div>
      </div>

      <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto scrollbar-custom">
          <table className="w-full text-sm text-left text-gray-300">
            <thead className="text-xs text-gray-500 uppercase bg-dark-surface border-b border-dark-border">
              <tr>
                <th className="px-6 py-4">Date</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Amount</th>
                <th className="px-6 py-4">Note</th>
                <th className="px-6 py-4">Receipt</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="6" className="px-6 py-8 text-center text-gray-500">Loading...</td></tr>
              ) : expenses.length === 0 ? (
                <tr><td colSpan="6" className="px-6 py-8 text-center text-gray-500">No expenses found.</td></tr>
              ) : (
                expenses.map(exp => (
                  <tr key={exp.id} className="border-b border-dark-border/50 hover:bg-dark-surface/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">{exp.expenseDate ? exp.expenseDate.split("T")[0] : '—'}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-danger/10 text-danger border border-danger/20">
                        {exp.categoryName}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-bold text-white">{formatCurrency(exp.amount)}</td>
                    <td className="px-6 py-4 truncate max-w-[150px]">{exp.note || '-'}</td>
                    <td className="px-6 py-4">
                      {exp.receiptUrl ? (
                        <a href={`/api/expenses/${exp.id}/receipt`} target="_blank" rel="noreferrer" className="text-primary-light hover:underline flex items-center">
                          <FileText className="w-4 h-4 mr-1" /> View
                        </a>
                      ) : (
                        <label className="cursor-pointer text-gray-400 hover:text-primary transition-colors flex items-center w-fit">
                          <Upload className="w-4 h-4 mr-1" /> Upload
                          <input
                            type="file"
                            className="hidden"
                            onChange={(e) => handleReceiptUpload(exp.id, e.target.files[0])}
                            accept="image/*,.pdf"
                          />
                        </label>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right flex justify-end gap-2">
                      <button onClick={() => openEditModal(exp)} className="text-gray-400 hover:text-primary transition-colors p-1">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(exp.id)} className="text-gray-400 hover:text-danger transition-colors p-1">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Main Expense Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={formData.id ? 'Edit Expense' : 'Add Expense'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Amount</label>
            <input type="number" step="0.01" required value={formData.amount} onChange={(e) => setFormData({ ...formData, amount: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" placeholder="0.00" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Category</label>
            <select required value={formData.categoryId} onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary">
              <option value="" disabled>Select a category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Date</label>
            <input 
              type="date" 
              required
              value={formData.date} 
              onChange={(e) => setFormData({ ...formData, date: e.target.value })} 
              className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary inline-block" 
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Note (Optional)</label>
            <textarea value={formData.note} onChange={(e) => setFormData({ ...formData, note: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" rows="3" />
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
            <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white">Cancel</button>
            <button type="submit" disabled={submitLoading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium">Save</button>
          </div>
        </form>
      </Modal>

      {/* Recurring Modal */}
      <Modal isOpen={isRecurringModalOpen} onClose={() => setIsRecurringModalOpen(false)} title="Setup Recurring Expense">
        <form onSubmit={handleRecurringSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Amount</label>
            <input type="number" step="0.01" required value={recurringData.amount} onChange={(e) => setRecurringData({ ...recurringData, amount: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" placeholder="0.00" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Category</label>
            <select required value={recurringData.categoryId} onChange={(e) => setRecurringData({ ...recurringData, categoryId: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary">
              <option value="" disabled>Select a category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Frequency</label>
            <select required value={recurringData.frequency} onChange={(e) => setRecurringData({ ...recurringData, frequency: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary">
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Start Date</label>
            <input 
              type="date" 
              required
              value={recurringData.startDate} 
              onChange={(e) => setRecurringData({ ...recurringData, startDate: e.target.value })} 
              className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary inline-block" 
            />
          </div>
          <div className="flex justify-end gap-2 pt-4 border-t border-dark-border mt-4">
            <button type="button" onClick={() => setIsRecurringModalOpen(false)} className="px-4 py-2 text-gray-400 hover:text-white">Cancel</button>
            <button type="submit" disabled={submitLoading} className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg disabled:opacity-50 font-medium">Save</button>
          </div>
        </form>
      </Modal>

    </div>
  );
}
