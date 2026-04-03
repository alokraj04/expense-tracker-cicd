import { useState, useEffect } from 'react';
import { incomeService, categoryService } from '../api';
import { Modal } from '../components/ui/Modal';
import { StatCard } from '../components/ui/StatCard';
import { Plus, Edit2, Trash2, ArrowUpFromLine, TrendingUp } from 'lucide-react';
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
export default function IncomesPage() {
  const [incomes, setIncomes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [summary, setSummary] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [formData, setFormData] = useState({
    id: null,
    amount: '',
    categoryId: '',
    date: new Date().toISOString().split('T')[0],
    description: ''
  });

  useEffect(() => {
    fetchCategories();
    fetchIncomes();
    fetchSummary();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await categoryService.getByType('INCOME');
      setCategories(res.data);
    } catch (error) {
      console.error("Categories fetch error", error);
    }
  };

  const fetchIncomes = async () => {
    setLoading(true);
    try {
      const res = await incomeService.getAll();
      console.log("INCOME DATA:", res.data);
      setIncomes(res.data);
    } catch (error) {
      toast.error('Failed to load incomes');
    } finally {
      setLoading(false);
    }
  };

  const fetchSummary = async () => {
    try {
      const res = await incomeService.getSummary();
      setSummary(res.data);
    } catch (error) {
      console.error("Summary fetch error", error);
    }
  };

  const openAddModal = () => {
    setFormData({ id: null, amount: '', categoryId: '', date: new Date().toISOString().split('T')[0], description: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (income) => {
    const targetDate = income.incomeDate || income.date || income.createdAt;
    setFormData({
      id: income.id,
      amount: income.amount,
      categoryId: income.categoryId || income.category?.id || '',
      date: targetDate ? targetDate.split('T')[0] : new Date().toISOString().split('T')[0],
      description: income.description || ''
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this income?')) return;
    try {
      await incomeService.delete(id);
      toast.success('Income deleted');
      fetchIncomes();
      fetchSummary();
    } catch (error) {
      toast.error('Failed to delete income');
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
          ...formData,
          amount: Number(formData.amount),
          categoryId: Number(formData.categoryId),
          incomeDate: formData.date ? new Date(formData.date).toISOString() : null
        };
        delete payload.date; // Remove generic date key as strictly requested
      } else {
        payload = {
          amount: Number(formData.amount),
          categoryId: Number(formData.categoryId),
          description: formData.description,
          incomeDate: formData.date ? new Date(formData.date).toISOString() : null
        };
      }

      console.log("FINAL PAYLOAD:", payload);

      if (formData.id) {
        const response = await incomeService.update(formData.id, payload);
        console.log("Updated Income:", response.data);
        const updatedIncome = response.data;
        if (updatedIncome) {
          setIncomes((prev) =>
            prev.map((item) => (item.id === updatedIncome.id ? updatedIncome : item))
          );
        }
        toast.success('Income updated');
      } else {
        await incomeService.create(payload);
        toast.success('Income added');
      }
      setIsModalOpen(false);
      await fetchIncomes();
      await fetchSummary();
      console.log("INCOME LIST:", incomes);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save income');
    } finally {
      setSubmitLoading(false);
    }
  };

  // Calculate total income shown in table
  const totalIncomeAmount = incomes.reduce((acc, curr) => acc + curr.amount, 0);

  return (
    <div className="space-y-6 slide-up">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold font-heading">Incomes</h1>
        <button
          onClick={openAddModal}
          className="bg-primary hover:bg-primary-hover text-white py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium w-fit"
        >
          <Plus className="w-4 h-4 mr-2" /> Add Income
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mb-6">
        {summary.slice(0, 3).map((item, index) => (
          <StatCard
            key={index}
            title={item.categoryName}
            value={formatCurrency(item.totalAmount)}
            subtitle={`${item.transactionCount} transactions`}
            icon={TrendingUp}
            colorClass="text-success"
          />
        ))}
      </div>

      <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto scrollbar-custom">
          <table className="w-full text-sm text-left text-gray-300">
            <thead className="text-xs text-gray-500 uppercase bg-dark-surface border-b border-dark-border">
              <tr>
                <th className="px-6 py-4">Date</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Description</th>
                <th className="px-6 py-4">Amount</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="5" className="px-6 py-8 text-center text-gray-500">Loading...</td></tr>
              ) : incomes.length === 0 ? (
                <tr><td colSpan="5" className="px-6 py-8 text-center text-gray-500">No incomes found.</td></tr>
              ) : (
                incomes.map(inc => (
                  <tr key={inc.id} className="border-b border-dark-border/50 hover:bg-dark-surface/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">{inc.incomeDate ? inc.incomeDate.split("T")[0] : '—'}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-success/10 text-success border border-success/20">
                        {inc.categoryName}
                      </span>
                    </td>
                    <td className="px-6 py-4 truncate max-w-[200px]">{inc.description || '-'}</td>
                    <td className="px-6 py-4 font-bold text-success">{formatCurrency(inc.amount)}</td>
                    <td className="px-6 py-4 text-right flex justify-end gap-2">
                      <button onClick={() => openEditModal(inc)} className="text-gray-400 hover:text-primary transition-colors p-1">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(inc.id)} className="text-gray-400 hover:text-danger transition-colors p-1">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
            {incomes.length > 0 && (
              <tfoot className="bg-dark-surface border-t border-dark-border">
                <tr>
                  <td colSpan="3" className="px-6 py-4 text-right font-medium text-gray-300">Total:</td>
                  <td className="px-6 py-4 font-bold text-success text-lg">{formatCurrency(totalIncomeAmount)}</td>
                  <td></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={formData.id ? 'Edit Income' : 'Add Income'}>
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
            <label className="block text-sm font-medium text-gray-300 mb-1">Description</label>
            <textarea value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-1 focus:ring-primary" rows="2" />
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
