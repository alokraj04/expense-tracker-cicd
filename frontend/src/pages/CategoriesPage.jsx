import { useState, useEffect } from 'react';
import { categoryService } from '../api';
import { Modal } from '../components/ui/Modal';
import { Plus, Edit2, Trash2, ArrowDownToLine, ArrowUpFromLine } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('EXPENSE'); // 'EXPENSE' or 'INCOME'
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({ id: null, name: '', categoryType: 'EXPENSE' });
  const [submitLoading, setSubmitLoading] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, [activeTab]);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await categoryService.getByType(activeTab);
      setCategories(res.data);
    } catch (error) {
      toast.error('Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({ id: null, name: '', categoryType: activeTab });
    setIsModalOpen(true);
  };

  const openEditModal = (category) => {
    setFormData({ id: category.id, name: category.name, categoryType: category.categoryType || activeTab });
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category?')) return;
    
    try {
      await categoryService.delete(id);
      toast.success('Category deleted');
      fetchCategories();
    } catch (error) {
      toast.error('Category is in use and cannot be deleted');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitLoading(true);
    try {
      if (formData.id) {
        await categoryService.update(formData.id, { name: formData.name, categoryType: formData.categoryType });
        toast.success('Category updated');
      } else {
        await categoryService.create({ name: formData.name, categoryType: formData.categoryType });
        toast.success('Category created');
      }
      setIsModalOpen(false);
      fetchCategories();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save category');
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div className="space-y-6 slide-up">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold font-heading">Categories</h1>
        <button 
          onClick={openAddModal}
          className="bg-primary hover:bg-primary-hover text-white py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium w-fit"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Category
        </button>
      </div>

      <div className="flex border-b border-dark-border mb-6">
        <button
          className={`px-4 py-2 font-medium text-sm focus:outline-none flex items-center transition-colors ${activeTab === 'EXPENSE' ? 'border-b-2 border-danger text-white' : 'text-gray-400 hover:text-gray-200'}`}
          onClick={() => setActiveTab('EXPENSE')}
        >
          <ArrowDownToLine className="w-4 h-4 mr-2" /> Expense Categories
        </button>
        <button
          className={`px-4 py-2 font-medium text-sm focus:outline-none flex items-center transition-colors ${activeTab === 'INCOME' ? 'border-b-2 border-success text-white' : 'text-gray-400 hover:text-gray-200'}`}
          onClick={() => setActiveTab('INCOME')}
        >
          <ArrowUpFromLine className="w-4 h-4 mr-2" /> Income Categories
        </button>
      </div>

      {loading ? (
        <div className="flex h-32 items-center justify-center text-gray-500">Loading components...</div>
      ) : categories.length === 0 ? (
        <div className="bg-dark-card border border-dark-border rounded-xl p-8 text-center text-gray-400">
          No categories found. Click Add Category to create one.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {categories.map((cat) => (
            <div key={cat.id} className="group relative bg-dark-card border border-dark-border rounded-xl p-5 hover:border-primary transition-colors flex items-center justify-between overflow-hidden shadow-sm">
              <div className="flex items-center">
                <div className={`p-2 rounded-lg mr-3 ${activeTab === 'EXPENSE' ? 'bg-danger/10 text-danger' : 'bg-success/10 text-success'}`}>
                  {activeTab === 'EXPENSE' ? <ArrowDownToLine className="w-5 h-5"/> : <ArrowUpFromLine className="w-5 h-5"/>}
                </div>
                <h3 className="font-medium text-white truncate max-w-[140px]">{cat.name}</h3>
              </div>
              
              <div className="opacity-0 group-hover:opacity-100 transition-opacity absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1 bg-dark-card pl-2">
                <button 
                  onClick={() => openEditModal(cat)}
                  className="p-1.5 text-gray-400 hover:text-primary transition-colors"
                >
                  <Edit2 className="w-4 h-4" />
                </button>
                <button 
                  onClick={() => handleDelete(cat.id)}
                  className="p-1.5 text-gray-400 hover:text-danger transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        title={formData.id ? 'Edit Category' : 'Add Category'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Category Name</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              className="w-full px-3 py-2 bg-dark-surface border border-dark-border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition"
              placeholder="e.g. Groceries, Salary..."
            />
          </div>
          
          <div className="pt-4 flex justify-end gap-2 border-t border-dark-border mt-6 text-sm">
             <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2text-gray-300 hover:text-white transition-colors"
             >
               Cancel
             </button>
             <button
              type="submit"
              disabled={submitLoading}
              className="bg-primary hover:bg-primary-hover text-white py-2 px-6 rounded-lg font-medium transition-colors disabled:opacity-50"
             >
               {submitLoading ? 'Saving...' : 'Save'}
             </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
