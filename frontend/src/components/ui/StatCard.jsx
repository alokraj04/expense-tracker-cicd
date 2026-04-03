export const StatCard = ({ title, value, icon: Icon, colorClass = "text-white", subtitle, className = "" }) => {
  return (
    <div className={`bg-dark-card border border-dark-border rounded-xl p-6 shadow-sm ${className}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-400 mb-1">{title}</p>
          <h3 className={`text-2xl font-bold font-heading ${colorClass}`}>
            {value}
          </h3>
          {subtitle && <p className="text-sm text-gray-500 mt-2">{subtitle}</p>}
        </div>
        {Icon && (
          <div className={`p-3 rounded-full bg-dark-surface ${colorClass}`}>
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>
    </div>
  );
};
