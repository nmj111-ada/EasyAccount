const BASE = '/api';

export interface Transaction {
  id: number; amount: number; type: number;
  categoryId: number; note: string; dateMs: number; createdAt: number;
}
export interface Category {
  id: number; name: string; icon: string; color: string; type: number; sortOrder: number;
}
export interface Budget { id: number; categoryId: number | null; monthlyLimit: number; }
export interface RecurringRule { id: number; categoryId: number; amount: number; type: number; note: string; periodType: number; nextDateMs: number; enabled: boolean; }

export async function fetchTransactions(startMs: number, endMs: number): Promise<Transaction[]> {
  const res = await fetch(`${BASE}/transactions/range?startMs=${startMs}&endMs=${endMs}`);
  return res.json();
}

export async function fetchCategoryStats(type: number, startMs: number, endMs: number): Promise<[number, number][]> {
  const res = await fetch(`${BASE}/stats/category?type=${type}&startMs=${startMs}&endMs=${endMs}`);
  return res.json();
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await fetch(`${BASE}/categories`);
  return res.json();
}
