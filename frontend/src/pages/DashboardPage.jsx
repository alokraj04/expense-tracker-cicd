import { useState, useEffect } from 'react';
import { budgetService, analyticsService } from '../api';
import { StatCard } from '../components/ui/StatCard';
import { ArrowDownToLine, ArrowUpFromLine, Wallet, CircleAlert, CheckCircle, AlertTriangle } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer, PieChart as RechartsPieChart, Pie, Cell } from 'recharts';
import toast from 'react-hot-toast';

const COLORS = ['#2D6A4F', '#52B788', '#40916C', '#1B4332', '#74C69D', '#95D5B2'];

const formatCurrency = (amount) => {
  if (amount == null) return "—";
  return amount.toLocaleString("en-IN", {
    style: "currency",
    currency: "INR",
  });
};

export default function DashboardPage() {
  const [dashboardData, setDashboardData] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    budgetStatus: 'OK'
  });
  const [incomeExpenseData, setIncomeExpenseData] = useState([]);
  const [categoryData, setCategoryData] = useState({ categories: [], totalAmount: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const fetchDashboard = async () => {
      setLoading(true);
      
      // Fetch Dashboard Stats
      try {
        const res = await budgetService.getDashboard();
        if (mounted) setDashboardData(res.data);
      } catch (error) {
        console.error("Dashboard stats error", error);
        toast.error("Could not load dashboard stats");
      }

      // Fetch Income vs Expense (Bar Chart)
      try {
        const res = await analyticsService.getIncomeExpense();
        if (mounted && Array.isArray(res.data)) {
          setIncomeExpenseData(res.data);
        }
      } catch (error) {
        console.error("Income/Expense chart error", error);
      }

      // Fetch Category Breakdown (Donut Chart)
      try {
        const res = await analyticsService.getCategoryBreakdown();
        if (mounted && res.data && Array.isArray(res.data.categories)) {
          setCategoryData(res.data);
        }
      } catch (error) {
        console.error("Category chart error", error);
      }

      if (mounted) setLoading(false);
    };

    fetchDashboard();

    return () => { mounted = false; };
  }, []);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OK': return <span className="flex items-center text-success"><CheckCircle className="w-4 h-4 mr-1"/> OK</span>;
      case 'WARNING': return <span className="flex items-center text-warning"><AlertTriangle className="w-4 h-4 mr-1"/> Warning</span>;
      case 'EXCEEDED': return <span className="flex items-center text-danger"><CircleAlert className="w-4 h-4 mr-1"/> Exceeded</span>;
      default: return null;
    }
  };

  if (loading) {
    return <div className="flex h-full items-center justify-center text-primary-light">Loading dashboard...</div>;
  }

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-dark-card border border-dark-border p-3 rounded shadow-lg text-sm">
          <p className="font-medium text-white mb-2">{label}</p>
          {payload.map((entry, index) => (
            <p key={index} style={{ color: entry.color }}>
              {entry.name}: {formatCurrency(entry.value)}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-heading">Dashboard</h1>
        <div className="bg-dark-card px-4 py-2 border border-dark-border rounded-lg shadow-sm flex items-center space-x-2">
          <span className="text-sm text-gray-400">Budget Status:</span>
          <span className="font-medium text-sm">{getStatusBadge(dashboardData.budgetStatus)}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard 
          title="Total Income" 
          value={formatCurrency(dashboardData.totalIncome)}
          icon={ArrowUpFromLine}
          colorClass="text-success"
        />
        <StatCard 
          title="Total Expense" 
          value={formatCurrency(dashboardData.totalExpense)}
          icon={ArrowDownToLine}
          colorClass="text-danger"
        />
        <StatCard 
          title="Net Balance" 
          value={formatCurrency(dashboardData.netBalance)}
          icon={Wallet}
          colorClass="text-primary-light"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bar Chart */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Income vs Expense (Monthly)</h3>
          {incomeExpenseData.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-gray-500">No data available</div>
          ) : (
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeExpenseData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1B4332" vertical={false} />
                  <XAxis dataKey="month" stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} />
                  <YAxis stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} />
                  <RechartsTooltip content={<CustomTooltip />} />
                  <Legend wrapperStyle={{ paddingTop: '20px' }} />
                  <Bar dataKey="totalIncome" name="Income" fill="#10B981" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="totalExpense" name="Expense" fill="#FF4D4D" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Donut Chart */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Category Detail (Expense)</h3>
          {categoryData.categories.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-gray-500">No data available</div>
          ) : (
            <div className="h-64 flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <RechartsPieChart>
                  <Pie
                    data={categoryData.categories}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={5}
                    dataKey="totalAmount"
                    nameKey="category"
                  >
                    {categoryData.categories.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <RechartsTooltip content={<CustomTooltip />} />
                  <Legend layout="vertical" verticalAlign="middle" align="right" wrapperStyle={{color: '#e5e7eb'}}/>
                </RechartsPieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
