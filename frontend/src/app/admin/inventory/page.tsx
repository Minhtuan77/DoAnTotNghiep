'use client';

import { useCallback, useEffect, useState } from 'react';
import { api } from '@/lib/api';
import type { Inventory } from '@/lib/types';

export default function InventoryPage() {
  const [items, setItems] = useState<Inventory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [adjustingId, setAdjustingId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const page: any = await api.inventory();
      setItems(Array.isArray(page?.content) ? page.content : []);
    } catch (e: any) {
      setItems([]);
      setError(e?.message || 'Không thể tải dữ liệu tồn kho.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const handleAdjust = async (item: Inventory) => {
    const raw = window.prompt(
      `Điều chỉnh tồn kho cho ${item.productName}.\nNhập số dương để tăng, số âm để giảm:`,
      '1',
    );

    if (raw === null) return;

    const changeQty = Number(raw.trim());
    if (!Number.isInteger(changeQty) || changeQty === 0) {
      window.alert('Số lượng thay đổi phải là số nguyên khác 0.');
      return;
    }

    if (changeQty < 0 && Math.abs(changeQty) > item.quantityOnHand) {
      window.alert('Không thể giảm nhiều hơn số lượng đang có trong kho.');
      return;
    }

    setAdjustingId(item.productId);
    try {
      await api.adjustInventory(item.productId, changeQty);
      await load();
    } catch (e: any) {
      window.alert(e?.message || 'Cập nhật tồn kho thất bại.');
    } finally {
      setAdjustingId(null);
    }
  };

  return (
    <>
      <div className="sectionHead">
        <div>
          <h1>Quản lý tồn kho</h1>
          <p>Số lượng thực tế, đã giữ và khả dụng theo Inventory API.</p>
        </div>
        <button className="secondary small" onClick={() => void load()} disabled={loading}>
          {loading ? 'Đang tải...' : 'Tải lại'}
        </button>
      </div>

      {error && (
        <div
          style={{
            marginBottom: 16,
            padding: '14px 16px',
            border: '1px solid #e4b8b8',
            borderRadius: 12,
            background: '#fff7f7',
            color: '#9d2424',
          }}
        >
          <b>Không tải được dữ liệu tồn kho.</b>
          <div style={{ marginTop: 4 }}>{error}</div>
        </div>
      )}

      <div className="tableWrap">
        <table className="table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>SKU</th>
              <th>Trong kho</th>
              <th>Đã giữ</th>
              <th>Khả dụng</th>
              <th>Ngưỡng thấp</th>
              <th>Điều chỉnh</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center', padding: 32 }}>
                  Đang tải dữ liệu tồn kho...
                </td>
              </tr>
            ) : items.length === 0 ? (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center', padding: 32, color: '#65736d' }}>
                  {error
                    ? 'Không có dữ liệu để hiển thị do API đang lỗi.'
                    : 'Chưa có dữ liệu tồn kho. Hãy kiểm tra bảng inventory trong cơ sở dữ liệu.'}
                </td>
              </tr>
            ) : (
              items.map((item) => (
                <tr key={item.productId}>
                  <td>
                    <b>{item.productName}</b>
                    {item.isLowStock && (
                      <div style={{ color: '#a52626', marginTop: 4 }}>Sắp hết hàng</div>
                    )}
                  </td>
                  <td>{item.productSku || '—'}</td>
                  <td>{item.quantityOnHand}</td>
                  <td>{item.quantityReserved}</td>
                  <td>{item.availableQuantity}</td>
                  <td>{item.lowStockThreshold}</td>
                  <td>
                    <button
                      className="secondary small"
                      onClick={() => void handleAdjust(item)}
                      disabled={adjustingId === item.productId}
                    >
                      {adjustingId === item.productId ? 'Đang cập nhật...' : 'Điều chỉnh'}
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
