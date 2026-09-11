import { useEffect, useState } from 'react'
import { fetchCategoryStats, fetchCategories, Category } from '../api'
import { PieChart, Pie, Cell, Tooltip, Legend } from 'recharts'

const COLORS = ['#FF7043','#42A5F5','#AB47BC','#26A69A','#5C6BC0','#EF5350','#66BB6A','#FFA726','#78909C','#8D6E63']

export default function Stats() {
  const [data, setData] = useState<{name:string,value:number}[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const start = new Date()
    start.setDate(1); start.setHours(0,0,0,0)
    const end = Date.now()

    Promise.all([
      fetchCategoryStats(0, start.getTime(), end),
      fetchCategories()
    ]).then(([stats, cats]) => {
      const items = stats.map(([catId, total]: [number, number]) => ({
        name: cats.find(c => c.id === catId)?.name ?? `分类#${catId}`,
        value: total
      }))
      setData(items)
      setLoading(false)
    })
  }, [])

  if (loading) return <div className="loading">加载中...</div>
  if (!data.length) return <div className="loading">暂无统计数据</div>

  return (
    <div className="card">
      <h2>分类支出分布</h2>
      <PieChart width={500} height={360}>
        <Pie
          data={data}
          dataKey="value"
          nameKey="name"
          cx="50%"
          cy="50%"
          outerRadius={110}
          labelLine={true}
          label={({ name, percent }) =>
            `${name} ${(percent! * 100).toFixed(1)}%`
          }
        >
          {data.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
        </Pie>
        <Tooltip formatter={(value: number) => `¥${value.toFixed(2)}`} />
        <Legend />
      </PieChart>
    </div>
  )
}
