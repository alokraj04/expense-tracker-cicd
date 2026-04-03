import { useState, useEffect } from 'react';
import { analyticsService } from '../api';
import { Download, PieChart as PieChartIcon } from 'lucide-react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer, PieChart as RechartsPieChart, Pie, Cell } from 'recharts';
import toast from 'react-hot-toast';

const COLORS = ['#2D6A4F', '#52B788', '#40916C', '#1B4332', '#74C69D', '#95D5B2', '#0A1A14', '#102A20', '#1B4332', '#173628'];

const formatCurrency = (value) => {
  if (value == null) return "₹0";
  return value.toLocaleString("en-IN", {
    style: "currency",
    currency: "INR",
  });
};

const formatMonth = (value) => {
  if (!value) return "";
  return new Date(value).toLocaleString("en-IN", { month: "short", year: "numeric" });
};

export default function AnalyticsPage() {
  const [monthlyExpense, setMonthlyExpense] = useState([]);
  const [weeklyExpense, setWeeklyExpense] = useState([]);
  const [incomeExpenseData, setIncomeExpenseData] = useState([]);
  const [categoryData, setCategoryData] = useState({ totalAmount: 0, categories: [] });
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    let mounted = true;
    
    const fetchAllData = async () => {
      setLoading(true);

      const fetchWithCatch = async (apiCall, setter, logName = null, transform = null) => {
        try {
          const res = await apiCall();
          if (logName) console.log(`${logName}:`, res.data);
          if (mounted && res.data) {
            const data = transform ? transform(res.data) : res.data;
            if (logName) console.log(`Chart data (${logName}):`, data);
            setter(data);
          }
        } catch (error) {
          console.error("Analytics fetch error", error);
        }
      };

      await Promise.all([
        fetchWithCatch(analyticsService.getMonthly, setMonthlyExpense, "MONTHLY DATA"),
        fetchWithCatch(analyticsService.getWeekly, setWeeklyExpense, "WEEKLY RAW DATA", (data) => {
           console.log("WEEKLY RAW RESPONSE:", data);

           const rawData =
              data?.data ||
              data?.weekly ||
              data ||
              [];

           const dataArray = Array.isArray(rawData) ? rawData : [];

           const formattedWeeklyData = dataArray.map((item) => ({
              day: item.week || (item.date ? new Date(item.date).toLocaleDateString("en-IN", { weekday: "short" }) : item.day) || "N/A",
              total: Number(item.totalExpense || item.amount || item.total || item.totalAmount || 0),
           }));
           
           console.log("WEEKLY FINAL:", formattedWeeklyData);
           return formattedWeeklyData;
        }),
        fetchWithCatch(analyticsService.getIncomeExpense, setIncomeExpenseData, "INCOME/EXPENSE DATA"),
        fetchWithCatch(analyticsService.getCategoryBreakdown, setCategoryData, "CATEGORY DATA")
      ]);

      if (mounted) setLoading(false);
    };

    fetchAllData();
    return () => { mounted = false; };
  }, []);

  const handleExport = async () => {
    setExporting(true);
    try {
      const res = await analyticsService.downloadReport();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'expense_report.xlsx');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success("Report downloaded successfully");
    } catch (error) {
      toast.error("Failed to download report");
    } finally {
      setExporting(false);
    }
  };

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

  if (loading) {
    return <div className="flex h-full items-center justify-center text-primary-light flex-col">
      <PieChartIcon className="w-8 h-8 animate-spin mb-4" />
      <span className="text-xl">Loading Analytics...</span>
    </div>;
  }

  return (
    <div className="space-y-6 slide-up">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h1 className="text-2xl font-bold font-heading">Analytics Details</h1>
        <button 
          onClick={handleExport}
          disabled={exporting}
          className="bg-primary hover:bg-primary-hover disabled:opacity-50 text-white py-2 px-4 rounded-lg flex items-center transition-colors text-sm font-medium w-fit"
        >
          <Download className="w-4 h-4 mr-2" />
          {exporting ? 'Exporting...' : 'Export Excel'}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Monthly Trend */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Monthly Expense Trend</h3>
          {!monthlyExpense || monthlyExpense.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-gray-500">No data available</div>
          ) : (
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={monthlyExpense}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1B4332" vertical={false} />
                  <XAxis dataKey="month" stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} tickFormatter={(val) => formatMonth(val)} />
                  <YAxis stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} tickFormatter={formatCurrency} />
                  <RechartsTooltip content={<CustomTooltip />} />
                  <Line type="monotone" dataKey="totalExpense" name="Expense" stroke="#FF4D4D" strokeWidth={3} dot={{ fill: '#FF4D4D', strokeWidth: 2 }} activeDot={{ r: 8 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Weekly Spending */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Weekly Spending</h3>
          {!weeklyExpense || weeklyExpense.length === 0 ? (
            <p className="h-64 flex items-center justify-center text-gray-500">No data available</p>
          ) : (
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={weeklyExpense}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1B4332" vertical={false} />
                  <XAxis dataKey="day" stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} />
                  <YAxis stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} tickFormatter={formatCurrency} />
                  <RechartsTooltip content={<CustomTooltip />} />
                  <Bar dataKey="total" name="Expense" fill="#F59E0B" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Income vs Expense */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Income vs Expense Variance</h3>
          {!incomeExpenseData || incomeExpenseData.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-gray-500">No data available</div>
          ) : (
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeExpenseData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1B4332" vertical={false} />
                  <XAxis dataKey="month" stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} tickFormatter={(val) => formatMonth(val)} />
                  <YAxis stroke="#9CA3AF" tick={{fill: '#9CA3AF'}} tickFormatter={formatCurrency} />
                  <RechartsTooltip content={<CustomTooltip />} />
                  <Legend wrapperStyle={{ paddingTop: '20px' }} />
                  <Bar dataKey="totalIncome" name="Income" fill="#10B981" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="totalExpense" name="Expense" fill="#FF4D4D" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Category Breakdown Donut */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm flex flex-col">
          <h3 className="text-lg font-heading font-semibold text-white mb-6">Category Breakdown</h3>
          {!categoryData || !categoryData.categories || categoryData.categories.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-gray-500 flex-1">No data available</div>
          ) : (
            <div className="flex-1 flex flex-col md:flex-row items-center justify-between gap-6">
              <div className="h-64 w-full md:w-1/2">
                <ResponsiveContainer width="100%" height="100%">
                  <RechartsPieChart>
                    <Pie
                      data={categoryData.categories}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="totalAmount"
                      nameKey="category"
                    >
                      {categoryData.categories.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <RechartsTooltip content={<CustomTooltip />} />
                  </RechartsPieChart>
                </ResponsiveContainer>
              </div>
              
              <div className="w-full md:w-1/2 max-h-64 overflow-y-auto scrollbar-custom pr-2">
                <table className="w-full text-sm text-left text-gray-300">
                  <thead className="text-xs text-gray-500 uppercase sticky top-0 bg-dark-card">
                    <tr>
                      <th className="py-2">Category</th>
                      <th className="py-2 text-right">%</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categoryData.categories.map((cat, i) => (
                      <tr key={i} className="border-b border-dark-border/50">
                        <td className="py-2 flex items-center">
                          <span className="w-3 h-3 rounded-full mr-2" style={{ backgroundColor: COLORS[i % COLORS.length] }}></span>
                          <span className="truncate max-w-[120px]">{cat.category}</span>
                        </td>
                        <td className="py-2 text-right font-medium">
                          {cat.percentage?.toFixed(1) || 0}%
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
           )}
        </div>
      </div>
    </div>
  );
}
