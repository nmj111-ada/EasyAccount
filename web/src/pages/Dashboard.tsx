import { useEffect, useState } from 'react'
import { fetchTransactions, fetchCategoryStats, fetchCategories, Transaction, Category } from '../api'

export default function Dashboard() {
  const [txs, setTxs] = useState<Transaction[]>([])
  const [cats, setCats] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const now = Date.now()
    const start = new Date()
    start.setDate(1); start.setHours(0,0,0,0)

    Promise.all([
      fetchTransactions(start.getTime(), now),
      fetchCategories()
    ]).then(([t, c]) => { setTxs(t); setCats(c); setLoading(false) })
  }, [])

  if (loading) return <div className="loading">加载中...</div>

  const totalExpense = txs.filter(t => t.type === 0).reduce((s, t) => s + t.amount, 0)
  const totalIncome = txs.filter(t => t.type === 1).reduce((s, t) => s + t.amount, 0)

  const catName = (id: number) => cats.find(c => c.id === id)?.name ?? `分类#${id}`

  return (
    <div>
      <div className="card">
        <h2>本月概览</h2>
        <div className="amount">¥{(totalIncome - totalExpense).toFixed(2)}</div>
        <div className="row">
          <div><div className="label">总收入</div><div className="val income">¥{totalIncome.toFixed(2)}</div></div>
          <div><div className="label">总支出</div><div className="val expense">¥{totalExpense.toFixed(2)}</div></div>
        </div>
      </div>

      <div className="card">
        <h2>最近账单</h2>
        <table>
          <thead><tr><th>日期</th><th>分类</th><th>备注</th><th>金额</th></tr></thead>
          <tbody>
            {txs.slice(0, 20).map(tx => (
              <tr key={tx.id}>
                <td>{new Date(tx.dateMs).toLocaleDateString()}</td>
                <td>{catName(tx.categoryId)}</td>
                <td>{tx.note || '-'}</td>
                <td className={tx.type === 0 ? 'expense' : 'income'}>
                  {tx.type === 0 ? '-' : '+'}¥{tx.amount.toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
