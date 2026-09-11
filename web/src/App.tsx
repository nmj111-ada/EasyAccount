import { useState } from 'react'
import Dashboard from './pages/Dashboard'
import Transactions from './pages/Transactions'
import Stats from './pages/Stats'

export default function App() {
  const [tab, setTab] = useState(0)
  const tabs = ['💰 仪表盘', '📒 流水明细', '📊 统计']

  return (
    <div className="app">
      <div className="header">
        <span style={{fontSize: 32}}>🍋</span>
        <h1>LemonBook</h1>
      </div>
      <div className="tabs">
        {tabs.map((t, i) => (
          <button key={i} className={tab === i ? 'active' : ''} onClick={() => setTab(i)}>{t}</button>
        ))}
      </div>
      {tab === 0 && <Dashboard />}
      {tab === 1 && <Transactions />}
      {tab === 2 && <Stats />}
    </div>
  )
}
