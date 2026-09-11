import { useEffect, useState } from 'react'
import { fetchTransactions, fetchCategories, Transaction, Category } from '../api'

export default function Transactions() {
  const [txs, setTxs] = useState<Transaction[]>([])
  const [cats, setCats] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const start = new Date()
    start.setFullYear(start.getFullYear() - 1)
    start.setMonth(0); start.setDate(1); start.setHours(0,0,0,0)
    const end = Date.now()

    Promise.all([
      fetchTransactions(start.getTime(), end),
      fetchCategories()
    ]).then(([t, c]) => { setTxs(t); setCats(c); setLoading(false) })
  }, [])

  if (loading) return <div className="loading">加载中...</div>
  const catName = (id: number) => cats.find(c => c.id === id)?.name ?? `分类#${id}`

  return (
    <div className="card">
      <h2>流水明细（近一年）</h2>
      <table>
        <thead><tr><th>日期</th><th>分类</th><th>备注</th><th>金额</th></tr></thead>
        <tbody>
          {txs.map(tx => (
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
  )
}
